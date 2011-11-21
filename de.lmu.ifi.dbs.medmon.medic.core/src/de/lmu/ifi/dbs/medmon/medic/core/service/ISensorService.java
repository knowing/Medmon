package de.lmu.ifi.dbs.medmon.medic.core.service;

import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;
import de.lmu.ifi.dbs.medmon.sensor.core.converter.IConverter;

public interface ISensorService {

	/**
	 * returns all registered ISensor services
	 * @return registered services
	 */
	public ISensor[] getSensors();
	
	/**
	 * returns the ISensor service with the passed id
	 * @param id
	 * @return
	 */
	public ISensor getSensor(String id);
	
	/**
	 * creates an ISensor Service which represent the sensor passed database object.
	 * This service can be used to access the sensor specifications and functions.
	 * @param sensor
	 * @return
	 */
	public ISensor loadSensorEntity(Sensor sensor);
	
	/**
	 * creates a IConverter for the passed sensor service.
	 * @param sensor
	 * @return
	 */
	public IConverter createConverter(ISensor sensor);
	
	/**
	 * creates a IConverter to read the data of the sensor represented by the passed sensor database object.
	 * @param sensor
	 * @return
	 */
	public IConverter createConverter(Sensor sensor);
	
}
