package de.lmu.ifi.dbs.medmon.medic.core.service;

import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.sensor.core.converter.IConverter;
import de.lmu.ifi.dbs.medmon.sensor.core.converter.ISensor;

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
	 * 
	 * @param sensor
	 * @return
	 */
	public ISensor loadSensorEntity(Sensor sensor);
	
	/**
	 * 
	 * @param sensor
	 * @return
	 */
	public IConverter createConverter(ISensor sensor);
	
	/**
	 * 
	 * @param sensor
	 * @return
	 */
	public IConverter createConverter(Sensor sensor);
	
}
