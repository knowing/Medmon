package de.lmu.ifi.dbs.medmon.sensor.core;

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

    /**
     * 
     * @param id
     * @return sensor or null
     */
    public ISensor getSensor(String id);
    
    /**
     * 
     * @return all available sensor with and without driver
     */
    public List<ISensor> getSensors();
    
    /**
     * 
     */
    //TODO create correct onSensorEvent method signature
    public void onSensorEvent();
    
    public void addListener(ISensorListener listener);
    
    public void removeListener(ISensorListener listener);
}
