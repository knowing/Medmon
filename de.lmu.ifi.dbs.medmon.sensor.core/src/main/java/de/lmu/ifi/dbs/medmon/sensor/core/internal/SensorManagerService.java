package de.lmu.ifi.dbs.medmon.sensor.core.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensorListener;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensorManager;
import de.lmu.ifi.dbs.medmon.sensor.core.watcher.LinuxWatcher;
import de.lmu.ifi.dbs.medmon.sensor.core.watcher.WindowsWatcher;

public class SensorManagerService implements ISensorManager {

    private Logger log = LoggerFactory.getLogger(ISensorManager.class);
    private Map<String, ISensor> sensors = new HashMap<>();
    private EventAdmin eventAdmin;

    private ExecutorService watchServiceExecutor;

    protected void activate(Map<String, String> properties) {
        log.info("SensorManagerService activated");
        // TODO start watching mass media devices

        String os = System.getProperty("os.name").toLowerCase();
        watchServiceExecutor = Executors.newSingleThreadExecutor();
        if (os.contains("linux")) {
            watchServiceExecutor.submit(new LinuxWatcher());
        } else if (os.contains("win")) {
            watchServiceExecutor.submit(new WindowsWatcher());
        } else {
            log.warn("Unkown OS");
        }

    }

    protected void deactivate(Map<String, String> properties) {
        log.info("SensorManagerService deactivated");
        // TODO stop watching mass media devices
        watchServiceExecutor.shutdownNow();
    }

    @Override
    public ISensor getSensor(String id) {
        return sensors.get(id);
    }

    @Override
    public List<ISensor> getSensors() {
        return Collections.unmodifiableList(new ArrayList<>(sensors.values()));
    }

    @Override
    public void onSensorEvent() {

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
        HashMap<String, Object> properties = new HashMap<>();
        properties.put(SENSOR_DATA, sensor);
        properties.put("org.eclipse.e4.data", sensor); // This is for e4
        eventAdmin.postEvent(new Event(SENSOR_TOPIC_ADD, properties));
        log.debug("Added sensor " + sensor.getId());
    }

    protected void unbindSensor(ISensor sensor) {
        sensors.remove(sensor.getId());
        HashMap<String, Object> properties = new HashMap<>();
        properties.put(SENSOR_DATA, sensor);
        properties.put("org.eclipse.e4.data", sensor); // This is for e4
        eventAdmin.postEvent(new Event(SENSOR_TOPIC_REMOVE, properties));
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
