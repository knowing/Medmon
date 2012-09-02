package de.lmu.ifi.dbs.medmon.sensor.core;

import org.osgi.service.device.Device;


/**
 * <p>Represents a real sensor</p>
 * 
 * @author Nepomuk Seiler, Stephan Picker
 * @version 1.0
 * @since 2011-11-15
 *
 */
public interface ISensor extends Device {

	/**
	 * returns the Id of the sendor
	 * @return
	 */
	public String getId();
	
	/**
	 * returns the name of the sensor
	 * @return
	 */
	public String getName();
	
	/**
	 * returns the description of the sensor
	 * @return
	 */
	public String getDescription();
	
	/**
	 * returns the version
	 * @return
	 */
	public String getSerial();
	
	/**
	 * 
	 * @return
	 */
	public Category getCategory();
	
	/**
	 * 
	 * @param driver
	 */
	public void setDriver(ISensorDriver driver);
	
	/**
	 * 
	 * @return
	 */
	public ISensorDriver getDriver();
	
	
}
