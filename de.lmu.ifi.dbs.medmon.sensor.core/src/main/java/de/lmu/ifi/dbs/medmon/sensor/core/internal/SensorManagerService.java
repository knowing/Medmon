package de.lmu.ifi.dbs.medmon.sensor.core.internal;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensorListener;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensorManager;
import de.lmu.ifi.dbs.medmon.sensor.core.watcher.IMassMediaWatcher;
import de.lmu.ifi.dbs.medmon.sensor.core.watcher.LinuxWatcher;
import de.lmu.ifi.dbs.medmon.sensor.core.watcher.WindowsWatcher;

public class SensorManagerService implements ISensorManager {

    private Logger log = LoggerFactory.getLogger(ISensorManager.class);

    /** Available sensor services */
    private Map<String, ISensor> sensors = new HashMap<>();

    /** Source -> Sensor mapping */
    private BiMap<Object, ISensor> sensorInstances = HashBiMap.create();

    /** Available sources */
    private List<Object> sources = new LinkedList<>();
    private EventAdmin eventAdmin;

    private ExecutorService watchServiceExecutor;

    protected void activate(Map<String, String> properties) {
        log.info("SensorManagerService activated");

        String os = System.getProperty("os.name").toLowerCase();
        watchServiceExecutor = Executors.newSingleThreadExecutor();
        IMassMediaWatcher watcher = null;
        if (os.contains("linux")) {
            watcher = new LinuxWatcher();
        } else if (os.contains("win")) {
            watcher = new WindowsWatcher();
        } else {
            log.warn("Unkown OS");
        }
        if (watcher != null) {
            watcher.setSensorManager(this);
            try {
                sources.addAll(watcher.getDevices());
            } catch (IOException e) {
                log.error("Not able to load sources", e);
            }
            watchServiceExecutor.submit(watcher);
        }

    }

    protected void deactivate(Map<String, String> properties) {
        log.info("SensorManagerService deactivated");
        watchServiceExecutor.shutdownNow();
    }

    @Override
    public List<ISensor> getSensors() {
        return Collections.unmodifiableList(new ArrayList<>(sensors.values()));
    }

    /* ============================================== */
    /* =========== Filesystem Event Handling ======== */
    /* ============================================== */

    @Override
    public void onSensorEvent(Path path, WatchEvent<Path> event) {
        if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
            sources.add(path);
            for (ISensor sensor : sensors.values()) {
                addSensorInstance(sensor, path);
            }
        } else if (event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
            sources.remove(path);
            removePath(path);
        }
    }

    private void addSensorInstance(ISensor sensor, Path path) {
        try {
            ISensor sensorInstance = sensor.create(path);
            if (sensor != null) {
                //TODO double adding of sensors
                ISensor oldSensor = sensorInstances.forcePut(path, sensorInstance);
                if (oldSensor != null) {
                    removeSensorInstance(oldSensor);
                }
                log.debug("Added sensorInstance " + sensorInstance);
            }
        } catch (IOException e) {
            // TODO handle exception
            log.error("Error on creating sensorInstance with source " + path, e);
        }

    }

    private void removeSensorInstance(ISensor sensorInstance) {
        if (!sensorInstances.containsValue(sensorInstance))
            return;
        Object source = sensorInstances.inverse().remove(sensorInstance);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put(SENSOR_DATA, sensorInstance);
        properties.put(SENSOR_SOURCE, source);
        postSensorEvent(properties);
        log.debug("Removed sensorInstance " + sensorInstance + " with source " + source);
    }

    private void removePath(Path path) {
        if (!sensorInstances.containsKey(path))
            return;
        ISensor removedSensor = sensorInstances.remove(path);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put(SENSOR_DATA, removedSensor);
        properties.put(SENSOR_SOURCE, path);
        postSensorEvent(properties);
        log.debug("Removed path " + path + " with instance " + removedSensor);
    }

    private void postSensorEvent(Map<String, Object> properties) {
        // This is for e4
        properties.put("org.eclipse.e4.data", properties.get(SENSOR_DATA));
        eventAdmin.postEvent(new Event(SENSOR_TOPIC_REMOVE, properties));
    }

    @Override
    public void addListener(ISensorListener listener) {
    }

    @Override
    public void removeListener(ISensorListener listener) {

    }

    /* ============================================== */
    /* =========== OSGi service binding ============= */
    /* ============================================== */

    protected void bindSensor(ISensor sensor) {
        sensors.put(sensor.getId(), sensor);
        log.debug("Added sensor " + sensor.getId());
        for (Object source : sources) {
            if (source instanceof Path) {
                addSensorInstance(sensor, (Path) source);
            }
        }
    }

    protected void unbindSensor(ISensor sensor) {
        sensors.remove(sensor.getId());
        for (ISensor sensorInstance : sensorInstances.values()) {
            removeSensorInstance(sensorInstance);
        }
        log.debug("Removed sensor " + sensor.getId());
    }

    protected void bindEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = eventAdmin;
        log.debug("Bind event admin");
    }

    protected void unbindEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = null;
        log.debug("Unbind event admin");
    }

}
