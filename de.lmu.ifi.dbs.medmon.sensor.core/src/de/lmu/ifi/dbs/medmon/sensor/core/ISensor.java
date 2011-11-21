package de.lmu.ifi.dbs.medmon.sensor.core;

import java.io.InputStream;


public interface ISensor {

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
	public String getVersion();
	
	/**
	 * returns the file prefix this sensor uses
	 * @return
	 */
	public String getFilePrefix();
	
	/**
	 * states weather the inputStream is convertible.
	 * if true, a Converter can be created with newConverter().
	 * @param inputStream
	 * @return
	 */
	public boolean isConvertable(InputStream inputStream);
	
	/**
	 * returns an instance of IConverter which provides access to the data of the sensor
	 * @param inputStream
	 * @return
	 */
	public IConverter newConverter(InputStream inputStream);
	
}
