package de.lmu.ifi.dbs.medmon.sensor.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * 
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 2011-11-20
 */
public abstract class URIConverter extends Converter {

	private final URI source;
	
	public URIConverter(URI source) throws IOException {
		super(source.toURL().openStream());
		this.source = source;
	}
	
	protected URIConverter(InputStream input) {
		super(input);
		source = null;
	}

	public void reset() throws IOException {
		if(source == null)
			throw new IOException("No URL given - URLConverter got initialized with InputStream");
		input.close();
		input = source.toURL().openStream();
	}
}
