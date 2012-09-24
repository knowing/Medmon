package de.lmu.ifi.dbs.medmon.sensor.core;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.List;

/**
 * 
 * @author Nepomuk Seiler
 * @version 1.0
 * 
 */
public interface ISensorManager {

    public static final String SENSOR_TOPIC = "medmon/sensor/core/sensor/*";
    public static final String SENSOR_TOPIC_ADD = "medmon/sensor/core/sensor/add";
    public static final String SENSOR_TOPIC_REMOVE = "medmon/sensor/core/sensor/remove";

    public static final String SENSOR_DATA = "medmon.sensor.core.sensor";
    public static final String SENSOR_SOURCE = "medmon.sensor.core.source";
    public static final String SENSOR_AVAILABLE = "medmon.sensor.core.sensor.available";

    /**
     * 
     * @return all available sensor services (no instances) with and without driver
     */
    public List<ISensor> getSensors();

    /**
     * 
     * @return all sensor instances which are connected to a data source
     */
    public List<ISensor> getConnectedSensors();

    /**
     * 
     */
    public void onSensorEvent(Path path, WatchEvent<Path> event);

    public void addListener(ISensorListener listener);

    public void removeListener(ISensorListener listener);
}
