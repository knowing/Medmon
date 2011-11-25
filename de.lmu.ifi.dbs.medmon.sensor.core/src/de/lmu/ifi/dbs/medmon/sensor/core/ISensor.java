package de.lmu.ifi.dbs.medmon.sensor.core;

import java.io.IOException;
import java.io.InputStream;


/**
 * <p>Represents a real sensor</p>
 * 
 * @author Nepomuk Seiler, Stephan Picker
 * @version 1.0
 * @since 2011-11-15
 *
 */
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
	 * states whether the inputStream is convertible.
	 * if true, a Converter can be created with newConverter().
	 * @param inputStream
	 * @return
	 * @throws IOException 
	 */
	public boolean isConvertable(InputStream inputStream) throws IOException;
	
	/**
	 * returns an instance of IConverter which provides access to the data of the sensor
	 * @param inputStream
	 * @return
	 * @throws IOException 
	 */
	public IConverter newConverter(InputStream inputStream) throws IOException;
	
}