package de.lmu.ifi.dbs.medmon.sensor.core.converter;

import java.io.InputStream;

import org.joda.time.Interval;

import weka.core.Instances;

public interface IConverter {

	/**
	 * returns the attributes of the sensor data
	 */
	public Instances getHeader();
	
	/**
	 * returns the data of the sensor
	 */
	public Instances getData();
	
	/**
	 * returns the time interval of the recorded sensor data
	 */
	public Interval getInterval();
	
	/**
	 * get the input stream
	 */
	public InputStream getInputStream();
	
}
