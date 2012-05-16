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
	
	public static final String SENSOR_TOPIC_CHANGED = "medmon/sensor/core/sensor/changed";
	
	public static final String SENSOR_PROPERTY_ADD = "medmon.sensor.core.sensor.added";
	public static final String SENSOR_PROPERTY_REMOVED = "medmon.sensor.core.sensor.removed";
	

	public List<ISensor> getSensors();
	
	public ISensor getSensor(String id);
}
