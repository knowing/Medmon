package de.lmu.ifi.dbs.medmon.sensor.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * 
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 2011-11-20
 */
public abstract class URLConverter extends Converter {

	private final URL source;
	
	public URLConverter(URL source) throws IOException {
		super(source.openStream());
		this.source = source;
	}
	
	protected URLConverter(InputStream input) {
		super(input);
		source = null;
	}

	public void reset() throws IOException {
		if(source == null)
			throw new IOException("No URL given - URLConverter got initialized with InputStream");
		input.close();
		input = source.openStream();
	}
}
