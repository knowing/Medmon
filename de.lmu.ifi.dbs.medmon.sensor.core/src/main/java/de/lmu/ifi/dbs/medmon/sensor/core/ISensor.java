package de.lmu.ifi.dbs.medmon.sensor.core;

import java.io.IOException;
import java.io.InputStream;

import org.joda.time.Interval;
import org.osgi.service.device.Device;

import weka.core.Instances;

/**
 * <p>
 * Represents a real sensor
 * </p>
 * 
 * @author Nepomuk Seiler, Stephan Picker
 * @version 1.0
 * @since 2011-11-15
 * 
 */
public interface ISensor extends Device {

    /**
     * returns the Id of the sendor
     * 
     * @return
     */
    public String getId();

    /**
     * returns the name of the sensor
     * 
     * @return
     */
    public String getName();

    /**
     * returns the description of the sensor
     * 
     * @return
     */
    public String getDescription();

    /**
     * returns the version
     * 
     * @return
     */
    public String getSerial();

    /**
     * 
     * @return
     */
    public Category getCategory();

    /**
     * @param driver
     */
    public void setDriver(ISensorDriver driver);

    /**
     * @return driver
     */
    public ISensorDriver getDriver();

    /**
     * <p>
     * Creates a instance of this sensor with the specified source. The source can be everything, e.g. Path, File,
     * String, URL, InputStream, etc.
     * </p>
     * 
     * @param source
     * @return object on success - null on wrong source
     * @throws IOException
     *             - on failure
     */
    public ISensor create(Object source) throws IOException;

    /**
     * Driver and source must be set.
     * 
     * @return dataset
     * @throws IOException
     */
    public Instances getData() throws IOException;

    /**
     * Works only on non-stream data
     * 
     * @return time interval of data
     * @throws IOException
     */
    public Interval getInterval() throws IOException;

    /**
     * Returns the datastream to the raw data
     * 
     * @return
     * @throws IOException
     */
    public InputStream getDataInputStream() throws IOException;

    /**
     * Defines if this is just the service or a valid instance
     * 
     * @return
     */
    public boolean isInstance();

}
