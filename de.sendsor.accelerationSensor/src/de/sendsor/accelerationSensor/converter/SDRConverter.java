package de.sendsor.accelerationSensor.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.AbstractFileLoader;
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil;
import de.lmu.ifi.dbs.medmon.sensor.core.container.Block;
import de.lmu.ifi.dbs.medmon.sensor.core.container.IBlock;
import de.lmu.ifi.dbs.medmon.sensor.core.converter.IConverter;

/**
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 01.04.2011
 * 
 */
public class SDRConverter extends AbstractFileLoader implements IConverter {

	/* ==== Factory ==== */
	public static final String ID = SDRLoader.class.getName();
	public static final String URL = "url";
	public static final String FILE = "file";

	/* ==== Loader ==== */
	public static String FILE_EXTENSION = ".sdr";

	private final static int BLOCKSIZE = 512;
	private final static int CONTENT_BLOCK = 504;
	private final static int MINUTEINBLOCKS = 9;
	private final static long TIME_CORRECTION_BEFORE = 7000; // Should be 7392 =
																// // 504 / 3 *
																// 44
	private final static long TIME_CORRECTION_AFTER = 44;

	private Instances dataset;

	private final Attribute timeAttribute;
	private Attribute xAttribute;
	private Attribute yAttribute;
	private Attribute zAttribute;

	private String interval = SDRLoaderFactory.INTERVAL_SECOND();
	private String aggregate = SDRLoaderFactory.AGGREGATE_AVERAGE();
	private int units = 1;

	public SDRConverter() {
		m_structure = ResultsUtil.timeSeriesResult(Arrays.asList(new String[] { "x", "y", "z" }));
		dataset = new Instances(m_structure);

		timeAttribute = dataset.attribute(ResultsUtil.ATTRIBUTE_TIMESTAMP());
		// initialize value attributes
		// TODO SDRConverter -> use ResultUtils.findValueAttributesAsJavaMap
		List<Attribute> valueAttributes = ResultsUtil.findValueAttributes(dataset);
		for (Attribute attribute : valueAttributes) {
			String name = attribute.getMetadata().getProperty(ResultsUtil.META_ATTRIBUTE_NAME());
			if (name.equals("x"))
				xAttribute = attribute;
			else if (name.equals("y"))
				yAttribute = attribute;
			else if (name.equals("z"))
				zAttribute = attribute;
		}
	}


	@Override
	public void setSource(InputStream input) throws IOException {
		if (!(input instanceof FileInputStream))
			return;
		// TODO SDRConverter -> implement setSource
		// super.setSource(input);
	}

	@Override
	public void setDirectory(String path) throws IOException {
		File dir = new File(path);
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(FILE_EXTENSION);
			}
		});
		if (files.length == 0)
			throw new IOException("No files found in this directory");
		// TODO SDRLoader - setDirectory warning if more than one file found
		// Setting the first file found
		setFile(files[0]);
	}

	@Override
	public Instances getStructure() throws IOException {
		return m_structure;
	}

	@Override
	public void copy(OutputStream out) throws IOException {
		// Assuming the sensor can't record future data
		copy(out, new Date(0), new Date());
	}

	@Override
	public void copy(OutputStream out, Date from, Date to) throws IOException {
		if (m_sourceFile == null)
			throw new IOException("No source file!");
		if (out == null)
			throw new IOException("No output!");

		// Initialize data
		byte[] data = new byte[BLOCKSIZE];

		// Initialize time handling
		Calendar date = new GregorianCalendar();
		Calendar last = new GregorianCalendar();
		boolean init = false;

		FileInputStream in = new FileInputStream(m_sourceFile);
		int read = in.read(data);
		System.out.println("Copy sensor data: " + from + " -> " + to);
		while (read != -1) {
			// Create timestamp
			int year = calcYear(data[506]);
			int month = data[507] - 1;
			int day = data[508];
			int hour = data[509];
			int minute = data[510];
			int second = data[511];

			date.set(year, month, day, hour, minute, second);
			if (!init) {
				init = true;
				last.set(year, month, day, hour, minute, second);
			}
			// Write data only "from >= date"
			if (!date.getTime().before(from) && date.after(last))
				out.write(data);
			/*
			 * Reasons for break 1) record ends when zeros appear 2) record ends
			 * when new date is before the previous entry 3) given parameter
			 * `to` is reached
			 */
			else if (recordEnd(day, hour) || date.before(last) || date.getTime().after(to))
				break;

			last.setTimeInMillis(date.getTimeInMillis());
			read = in.read(data);
		}
		in.close();
	}

	@Override
	public IBlock convert() throws IOException {
		if (m_sourceFile == null)
			throw new IOException("No source file!");

		// Initialize data
		byte[] data = new byte[BLOCKSIZE];

		// Initialize time handling
		Calendar first = new GregorianCalendar();
		Calendar date = new GregorianCalendar();
		Calendar last = new GregorianCalendar();
		boolean init = false;

		FileInputStream in = new FileInputStream(m_sourceFile);
		int read = in.read(data);
		long length = 0;
		while (read != -1) {
			// Create timestamp
			int year = calcYear(data[506]);
			int month = data[507] - 1;
			int day = data[508];
			int hour = data[509];
			int minute = data[510];
			int second = data[511];

			date.set(year, month, day, hour, minute, second);
			if (!init) {
				init = true;
				last.set(year, month, day, hour, minute, second);
				first.set(year, month, day, hour, minute, second);
			}
			// Checks if the recorded data ended or "date < to"
			if (recordEnd(day, hour) || date.before(last))
				break;

			last.setTimeInMillis(date.getTimeInMillis());
			length += read;
			read = in.read(data);
		}
		in.close();
		Block block = new Block(length, first.getTime(), last.getTime());
		block.setPath(m_File);
		return block;
	}

	@Override
	public Instances getDataSet() throws IOException {
		if (!dataset.isEmpty())
			return dataset;
		if (m_sourceFile == null)
			throw new IOException("No source file!");
		
		// Initialize position handling
		byte[] data = new byte[BLOCKSIZE];

		// Initialize time handling
		Calendar date = new GregorianCalendar();
		Calendar timestamp = new GregorianCalendar();

		Calendar intervalstart = new GregorianCalendar();
		Calendar intervalcurrent = new GregorianCalendar();
		// Initialize data handling

		FileInputStream in = new FileInputStream(m_sourceFile);
		int read = in.read(data);

		int avg_x = 0;
		int avg_y = 0;
		int avg_z = 0;
		boolean newInterval = true;
		long interval = getIntervalLength();
		if(units > 0)
			interval *= units;

		// Convert each block
		while (read != -1) {
			// Load Data into data-Buffer
			in.read(data, 0, BLOCKSIZE);

			// Create timestamp
			int year = calcYear(data[506]);
			int month = data[507] - 1;
			int day = data[508];
			int hour = data[509];
			int minute = data[510];
			int second = data[511];
			date.set(year, month, day, hour, minute, second);
			long time = date.getTimeInMillis() - TIME_CORRECTION_BEFORE;

			// Checks if the recorded data ended 
			if (recordEnd(day, hour))
				break;
			
			//Fill in the data
			for (int j = 0; j < CONTENT_BLOCK; j += 3) {
				timestamp.setTimeInMillis(time);
				int x = data[j];
				int y = data[j + 1];
				int z = data[j + 2];

				//for the very first run
				if (newInterval) {
					avg_x = x;
					avg_y = y;
					avg_z = z;
					newInterval = false;
					intervalstart.setTimeInMillis(time);
					intervalcurrent.setTimeInMillis(time);
				}
				//Increase time interval
				time += TIME_CORRECTION_AFTER;
				if (intervalcurrent.getTimeInMillis()-intervalstart.getTimeInMillis() < interval) {
					//Still in our time interval bounds
					avg_x = (avg_x + x) / 2;
					avg_y = (avg_y + y) / 2;
					avg_z = (avg_z + z) / 2;
					intervalcurrent.setTimeInMillis(time);
				} else {
					//New interval begins, save old one
					DenseInstance instance = new DenseInstance(4);
					instance.setValue(timeAttribute, timestamp.getTimeInMillis());
					instance.setValue(xAttribute, avg_x);
					instance.setValue(yAttribute, avg_y);
					instance.setValue(zAttribute, avg_z);
					dataset.add(instance);
					avg_x = x;
					avg_y = y;
					avg_z = z;
					intervalstart.setTimeInMillis(time);
					intervalcurrent.setTimeInMillis(time);
				}

			}
			read = in.read(data);

		}
		in.close();
		return dataset;
	}

	/**
	 * Calculate the complete year from the incomplete data of file
	 * 
	 * @param yearIncomlpete
	 * @return the complete year
	 */
	private static int calcYear(int yearIncomlpete) {
		int year = 0;
		if (yearIncomlpete >= 0)
			year = 2000 + yearIncomlpete;
		else
			year = 2000 + 127 + Math.abs(yearIncomlpete);

		return year;
	}

	/**
	 * Currently a SDR file is a bunch of zeros. Those zeros are placeholders
	 * and will be overwritten. This method checks if the end of the recorded data
	 * is reached, however not the end of the file.
	 * 
	 * @param day
	 * @param hour
	 * @return
	 */
	private boolean recordEnd(int day, int hour) {
		return (day == 48) && (hour == 48);
	}

	private long getIntervalLength() {
		if (interval.equals(SDRLoaderFactory.INTERVAL_SECOND()))
			return TimeUnit.SECONDS.toMillis(1);
		else if (interval.equals(SDRLoaderFactory.INTERVAL_MINUTE()))
			return TimeUnit.MINUTES.toMillis(1);
		else if (interval.equals(SDRLoaderFactory.INTERVAL_HOUR()))
			return TimeUnit.HOURS.toMillis(1);
		else if (interval.equals(SDRLoaderFactory.INTERVAL_DAY()))
			return TimeUnit.DAYS.toMillis(1);
		else
			return TimeUnit.SECONDS.toMillis(1);
	}

	@Override
	public Instance getNextInstance(Instances structure) throws IOException {
		return null;
	}
	
	public void setInterval(String interval) {
		this.interval = interval;
	}

	public void setAggregate(String aggregate) {
		this.aggregate = aggregate;
	}
	
	public void setUnits(int units) {
		this.units = units;
	}

	@Override
	public String getFileExtension() {
		return FILE_EXTENSION;
	}

	@Override
	public String[] getFileExtensions() {
		return new String[] { FILE_EXTENSION };
	}

	@Override
	public String getFileDescription() {
		return "Sendsor - Acceleration Input Format";
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getRevision() {
		return "";
	}

}
