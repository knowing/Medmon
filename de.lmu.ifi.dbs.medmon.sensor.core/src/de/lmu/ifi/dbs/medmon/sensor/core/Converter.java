package de.lmu.ifi.dbs.medmon.sensor.core;

import java.io.InputStream;

/**
 * 
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 2011-11-20
 */
public abstract class Converter implements IConverter {

	protected InputStream input;

	public Converter(InputStream input) {
		this.input = input;
	}

	@Override
	public InputStream getInputStream() {
		return input;
	}

}
