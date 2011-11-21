package de.lmu.ifi.dbs.medmon.sensor.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <p>Handles InputStreams from the filesystem. Is resetable.</p>
 * 
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 2011-11-20
 */
public abstract class FileConverter extends URLConverter {

	private final Path source;

	public FileConverter(Path source) throws IOException {
		super(Files.newInputStream(source));
		this.source = source;
	}

	public FileConverter(File file) throws IOException {
		this(file.toPath());
	}

	public FileConverter(String file) throws IOException {
		this(Paths.get(file));
	}

	protected FileConverter(InputStream input) {
		super(input);
		source = null;
	}
	
	public FileConverter(URL source) throws IOException {
		super(source.openStream());
		this.source = null;
	}

	/**
	 * <p>Tries to reset the InputStream based on the
	 * given path. If no path is given, tries super.reset()
	 * to reset InputStream based on the URL.</p>
	 */
	@Override
	public void reset() throws IOException {
		if (source == null) {
			super.reset();
		} else {
			input.close();
			input = Files.newInputStream(source);
		}
	}

}
