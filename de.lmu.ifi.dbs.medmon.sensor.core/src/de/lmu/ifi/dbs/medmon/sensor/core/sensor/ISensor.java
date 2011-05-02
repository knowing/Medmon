package de.lmu.ifi.dbs.medmon.sensor.core.sensor;

import java.io.File;
import java.io.IOException;

import weka.core.Instances;
import de.lmu.ifi.dbs.medmon.sensor.core.converter.IConverter;

/**
 * Representing a generic sensor
 * 
 * @author Nepomuk Seiler
 * @version 0.9
 */
public interface ISensor {

	public static final String SENSOR_ID = "de.lmu.ifi.dbs.medmon.sensor";
	public static final int MASTER = 1;
	public static final int SLAVE = 2;
	
	public String getId();
	
	public String getVersion();
	
	public String getName();
	
	public String getDescription();
	
	public int getType();
	
	/**
	 * 
	 * 
	 * @param path - should be the sensor directory.
	 * @return ISensorDataContainer
	 * @throws IOException
	 */
	public Instances getData(String path) throws IOException;
	
	public IConverter getConverter();
	
	/**
	 * 
	 * @param dir
	 * @return
	 */
	public boolean isSensor(File dir);
	
}
