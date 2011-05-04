package de.lmu.ifi.dbs.medmon.sensor.core.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import de.lmu.ifi.dbs.medmon.sensor.core.container.IBlock;

import weka.core.Instances;
import weka.core.converters.BatchConverter;
import weka.core.converters.FileSourcedConverter;
import weka.core.converters.Loader;

/**
 * <p>Converts sensor data into {@link Instances}
 * 
 * @author Nepomuk Seiler
 * @version 0.3
 */
public interface IConverter extends Loader, BatchConverter {
	
	/**
	 * 
	 * @param path
	 */
	void setDirectory(String path) throws IOException;
	
	/**
	 * 
	 * @param out
	 * @throws IOException
	 */
	void copy(OutputStream out) throws IOException;
	
	/**
	 * 
	 * @param out
	 * @param from
	 * @param to
	 * @throws IOException
	 */
	void copy(OutputStream out, Date from, Date to) throws IOException;
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	IBlock convert() throws IOException;
	
	/**
	 * 
	 * @return 
	 */
	String getFileExtension();
	
}
