package de.lmu.ifi.dbs.medmon.medic.core.service;

import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.sensor.core.IConverter;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

/**
 * <p>This OSGi service serve three purposes
 * <li>ISensor directory</li>
 * <li>SensorUtil to easy create IConverter</li>
 * <li>later: WatchService to react on plugged sensors</li>
 * 
 * </p>
 * 
 * @author Nepomuk Seiler, Stephan Picker
 * @version 1.0
 * @since 2011-11-15
 *
 */
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
	public Sensor loadSensorEntity(ISensor sensor);
	
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
