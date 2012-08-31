package de.lmu.ifi.dbs.medmon.sensor.core;

import java.util.List;

/**
 * 
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 2012-05-16
 *
 */
public interface ISensorDirectoryService {
	
	public static final String SENSOR_TOPIC = "medmon/sensor/core/sensor/*";
	public static final String SENSOR_TOPIC_ADD = "medmon/sensor/core/sensor/add";
	public static final String SENSOR_TOPIC_REMOVE = "medmon/sensor/core/sensor/remove";
	
	public static final String SENSOR_DATA = "medmon.sensor.core.sensor";
	

	public List<ISensor> getSensors();
	
	public ISensor getSensor(String id);
}
