package de.lmu.ifi.dbs.medmon.sensor.core.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import de.lmu.ifi.dbs.medmon.sensor.core.container.IBlock;

import weka.core.converters.BatchConverter;
import weka.core.converters.Loader;

/**
 * @author Nepomuk Seiler
 *
 * @version 0.9
 */
public interface IConverter extends Loader, BatchConverter {
	
	void copy(OutputStream out, Date from, Date to) throws IOException;
	
	IBlock convert() throws IOException;
	
}
