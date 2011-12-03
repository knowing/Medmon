package de.sendsor;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import weka.core.Instances;
import weka.core.converters.ArffSaver;

/**
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 19.06.2011
 * 
 */
public class Persister {

	private static final String sep = System.getProperty("file.separator");

	private final Date from;
	private final Date to;
	private final boolean relative;
	private final SDRConverter converter;

	/**
	 * <p>
	 * Converter must have a valid sourceFile
	 * </p>
	 * 
	 * @param from
	 *            - from which date to persist
	 * @param to
	 *            - to which date to persist
	 * @param converter
	 */
	public Persister(Date from, Date to, SDRConverter converter,
			boolean relative) {
		this.from = from;
		this.to = to;
		this.relative = relative;
		this.converter = converter;
	}

	/**
	 * @param listener
	 * @param path
	 *            - output file
	 * @throws IOException
	 */
	public void persistAsCSV(File file, PropertyChangeListener listener)
			throws IOException {
		Instances dataset = convert(file.getAbsolutePath());
		CSVSaver saver = new CSVSaver();
		saver.addPropertyChangeListener(listener);
		saver.setFile(file);
		saver.setInstances(dataset);
		saver.setSeparator(",");
		saver.writeBatch();
	}

	/**
	 * @param file
	 *            - output file
	 * @throws IOException
	 */
	public void persistAsARFF(File file) throws IOException {
		Instances dataset = convert(file.getAbsolutePath());
		ArffSaver saver = new ArffSaver();
		saver.setFile(file);
		saver.setInstances(dataset);
		saver.writeBatch();
	}

	private Instances convert(String path) throws IOException {
		converter.setAggregate(SDRConverter.AGGREGATE_NONE);
		converter.setRelativeTimestamp(relative);
		Instances returns = converter.getDataSet();
		return returns;
	}

	private String directory(String path) {
		int index = path.lastIndexOf(sep);
		if (index == -1)
			return path;
		return path.substring(0, index);
	}
}
