package de.lmu.ifi.dbs.medmon.sensor.core;

import java.io.IOException;
import java.io.InputStream;

import org.joda.time.Interval;
import org.osgi.service.device.Driver;

import weka.core.Instances;

/**
 * <p>Converts sensor-data into {@link Instances}.</p>
 * 
 * @author Nepomuk Seiler, Stephan Picker
 * @version 1.0
 * @since 2011-11-15
 *
 */
public interface ISensorDriver extends Driver {

	/**
	 * returns the data of the sensor.
	 * 
	 * After invoking this method the converter will close the
	 * InputStream. 
	 * 
	 * @return data located in the InputStream
	 */
	public Instances getData(InputStream in) throws IOException;
	
	/**
	 * returns the time interval of the recorded sensor data.
	 * 
	 * After invoking this method the converter will close the
	 * InputStream. 
	 * 
	 * @return time interval of the data in the InputStream
	 */
	public Interval getInterval(InputStream in) throws IOException;
	
	/**
	 * returns the attributes of the sensor data
	 */
	public Instances getHeader();
	
	/**
	 * returns the file prefix this sensor uses
	 * @return
	 */
	public String getFilePrefix();
}
