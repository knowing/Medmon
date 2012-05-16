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

	public List<ISensor> getSensors();
	
	public ISensor getSensor(String id);
}
