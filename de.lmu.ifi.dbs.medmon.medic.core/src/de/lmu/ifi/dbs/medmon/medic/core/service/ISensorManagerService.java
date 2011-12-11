package de.lmu.ifi.dbs.medmon.medic.core.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.sensor.core.IConverter;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

/**
 * <p>
 * This OSGi service serve three purposes
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
public interface ISensorManagerService {

	/**
	 * returns all registered ISensor services
	 * 
	 * @return registered services
	 */
	public ISensor[] getSensors();

	/**
	 * returns the ISensor service with the passed id
	 * 
	 * @param id
	 * @return
	 */
	public ISensor getSensor(String id);

	/**
	 * notifies all SensorObservers that a Sensor Service has been updated
	 * @param sensor
	 */
	public void notifySensorObservers(ISensor sensor);

	/**
	 * creates an ISensor Service which represent the sensor passed database
	 * object. This service can be used to access the sensor specifications and
	 * functions.
	 * 
	 * @param sensor
	 * @return
	 */
	public Sensor loadSensorEntity(ISensor sensor);

	/**
	 * return the SensorService belonging to the entity
	 * 
	 * @param sensor
	 * @return
	 */
	public ISensor loadSensorService(Sensor sensor);

	/**
	 * creates a IConverter for the passed sensor service.
	 * 
	 * @param sensor
	 * @return
	 * @throws IOException
	 */

	public IConverter createConverter(ISensor sensor) throws IOException;

	/**
	 * This method is used to copy from a sensor to another location. of
	 * _ISensor_ as a parameter.
	 * 
	 * @param sensor
	 * @return
	 */
	// public InputStream createInputStream(ISensor sensor);

	/**
	 * This method is used if a sensor has more than one inputStream (serveral
	 * files, a folder of raw data, etc). The array contains an URL to every
	 * resource that is a valid sensor data.
	 * 
	 * @return
	 */
	public URI[] availableInputs(ISensor sensor);

	/**
	 * Create an InputStream/IConverter based on the URI.
	 * 
	 * @param sensor
	 * @param inputIndex
	 * @return
	 * @throws IOException
	 */
	public InputStream createInput(ISensor sensor, URI uri) throws IOException;

	/**
	 * Create InputStreams/IConverters for all valid resources at the sensor
	 * location.
	 * 
	 * @param sensor
	 * @return
	 * @throws IOException
	 */
	public InputStream[] createInputs(ISensor sensor) throws IOException;

	/**
	 * Create the Default InputStream/IConverter
	 * 
	 * @param sensor
	 * @return
	 * @throws IOException
	 */
	public InputStream createDefaultInput(ISensor sensor) throws IOException;

}