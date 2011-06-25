package de.sendsor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import weka.core.converters.ArffSaver;

import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil;

/**
 * <p>
 * Converts SDR files into ARFF format or copies the binary to a specified
 * outputstream
 * </p>
 * 
 * @author Nepomuk Seiler, Christian Moennig
 * @version 0.4
 * @since 01.04.2011
 * 
 */
public class SDRConverter extends AbstractFileLoader {

	/* ==== Configuration Options ==== */
	public static final String URL = "url";
	public static final String FILE = "file";

	public static final String AGGREGATE = "aggregate";
	public static final String INTERVAL = "interval";
	public static final String UNITS = "units";
	public static final String RELATIVE_TIMESTAMP = "timestamp";
	public static final String OUTPUT = "output";

	public static final String AGGREGATE_NONE = "none";
	public static final String AGGREGATE_AVERAGE = "average"; // Default
	public static final String AGGREGATE_INTERVAL_FIRST = "interval_first";
	public static final String AGGREGATE_INTERVAL_LAST = "interval_last";

	public static final String INTERVAL_SECOND = "second"; // Default
	public static final String INTERVAL_MINUTE = "minute";
	public static final String INTERVAL_HOUR = "hour";
	public static final String INTERVAL_DAY = "day";

	public static final String RELATIVE_TIMESTAMP_RELATIVE = "relative";
	public static final String RELATIVE_TIMESTAMP_ABSOLUTE = "absolute"; // Default

	/* ==== Loader ==== */
	public static String FILE_EXTENSION = ".sdr";

	private final static int BLOCKSIZE = 512;
	private final static int CONTENT_BLOCK = 504;

	/** 168 instances / 25Hz = 6720 ms */
	private final static long TIME_CORRECTION_BEFORE = 6720;
	/** 1 / 25Hz = 40ms */
	private final static long SAMPLE_DISTANCE = 40;

	private Instances dataset;

	private Attribute timeAttribute;
	private Attribute xAttribute;
	private Attribute yAttribute;
	private Attribute zAttribute;

	/* == OPTIONS == */
	private String interval = INTERVAL_SECOND;
	private String aggregate = AGGREGATE_AVERAGE;
	private double units = 1.0;
	private boolean relativeTimestamp = false;

	/* */
	private boolean firstRun = true;

	public SDRConverter() {
		initStructure();
	}
	
	private void initStructure() {
		m_structure = ResultsUtil.timeSeriesResult(Arrays.asList(new String[] { "x", "y", "z" }), "yyyy-MM-dd HH:mm:ss.SSS");
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
	public void reset() throws IOException {
		super.reset();
		initStructure();
	}

	public void copy(OutputStream out) throws IOException {
		// Assuming the sensor can't record future data
		copy(out, new Date(0), new Date());
	}

	public void copy(OutputStream out, Date from, Date to) throws IOException {
		if (m_sourceFile == null)
			throw new IOException("No source file!");
		if (out == null)
			throw new IOException("No output!");

		// Initialize data
		byte[] data = new byte[BLOCKSIZE];

		// Initialize time handling
		Calendar date = new GregorianCalendar();
		Calendar prev = new GregorianCalendar();
		boolean init = false;

		FileInputStream in = new FileInputStream(m_sourceFile);
		int read = in.read(data);
		System.out.println("Copy sensor data: " + from + " -> " + to);
		while (read != -1) {
			// Create timestamp
			boolean recordEnd = setTime(date, data);
			if (!init) {
				init = true;
				prev.setTimeInMillis(date.getTimeInMillis());
			}
			// Write data only "from >= date"
			if (date.getTime().after(from) && date.after(prev)) 
				out.write(data);
				
			/*
			 * Reasons for break 1) record ends when zeros appear 2) record ends
			 * when new date is before the previous entry 3) given parameter
			 * `to` is reached
			 */
			if (recordEnd || date.before(prev) || date.getTime().after(to))
				break;

			prev.setTimeInMillis(date.getTimeInMillis());
			read = in.read(data);
		}
		in.close();
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
		if (units > 0)
			interval *= units;

		// Convert each block
		while (read != -1) {
			// Load Data into data-Buffer
			in.read(data, 0, BLOCKSIZE);

			// Create timestamp
			boolean recordEnd = setTime(date, data);
			// For relative time handling
			long time = date.getTimeInMillis();
			
			// Checks if the recorded data ended
			if (recordEnd)
				break;

			// Fill in the data
			for (int j = 0; j < CONTENT_BLOCK; j += 3) {
				int x = data[j];
				int y = data[j + 1];
				int z = data[j + 2];

				// for the very first run
				if (newInterval) {
					avg_x = x;
					avg_y = y;
					avg_z = z;
					newInterval = false;
					intervalstart.setTimeInMillis(time);
					intervalcurrent.setTimeInMillis(time);
				}
				boolean insideBounds = intervalcurrent.getTimeInMillis() - intervalstart.getTimeInMillis() < interval;
				if (aggregate.equals("none") || !insideBounds) {
					// New interval begins, save old one
					DenseInstance instance = new DenseInstance(4);
					instance.setValue(timeAttribute, time);
					instance.setValue(xAttribute, avg_x);
					instance.setValue(yAttribute, avg_y);
					instance.setValue(zAttribute, avg_z);
					dataset.add(instance);
					avg_x = x;
					avg_y = y;
					avg_z = z;
					intervalstart.setTimeInMillis(time);
					intervalcurrent.setTimeInMillis(time);
				} else if (insideBounds) {
					// Still in our time interval bounds
					avg_x = (avg_x + x) / 2;
					avg_y = (avg_y + y) / 2;
					avg_z = (avg_z + z) / 2;
					intervalcurrent.setTimeInMillis(time);
				}
				// Increase time interval
				time += SAMPLE_DISTANCE;
			}
			date.setTimeInMillis(time);
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
	 * <p>
	 * Set the calendar object with the given SDR-Byte array and checks if the
	 * current timestamp is zero (end of file)
	 * </p>
	 * 
	 * <p>
	 * If option relativeTimestamp is set this method does nothing except on the
	 * first run and initially sets the timestamp.
	 * </p>
	 * 
	 * @param calendar
	 *            - this object will be changed
	 * @param data
	 *            - byte array
	 * @return boolean - end of file reached
	 */
	private boolean setTime(Calendar calendar, byte[] data) {
		int year = calcYear(data[506]);
		int month = data[507] - 1;
		int day = data[508];
		int hour = data[509];
		int minute = data[510];
		int second = data[511];
		// Set date only on first run or on absolute timestamps
		if (firstRun || !relativeTimestamp) {
			calendar.set(year, month, day, hour, minute, second);
			long time = calendar.getTimeInMillis() - TIME_CORRECTION_BEFORE;
			calendar.setTimeInMillis(time);
			firstRun = false;
		} else {
			calendar.setTimeInMillis(calendar.getTimeInMillis() + SAMPLE_DISTANCE);
		}
		return recordEnd(day, hour);
	}

	/**
	 * Currently a SDR file is a bunch of zeros. Those zeros are placeholders
	 * and will be overwritten. This method checks if the end of the recorded
	 * data is reached, however not the end of the file.
	 * 
	 * @param day
	 * @param hour
	 * @return
	 */
	private boolean recordEnd(int day, int hour) {
		return (day == 48) && (hour == 48);
	}

	/**
	 * 
	 * @return
	 */
	private long getIntervalLength() {
		if (interval.equals(INTERVAL_SECOND))
			return TimeUnit.SECONDS.toMillis(1);
		else if (interval.equals(INTERVAL_MINUTE))
			return TimeUnit.MINUTES.toMillis(1);
		else if (interval.equals(INTERVAL_HOUR))
			return TimeUnit.HOURS.toMillis(1);
		else if (interval.equals(INTERVAL_DAY))
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

	public void setUnits(double units) {
		this.units = units;
	}

	public void setRelativeTimestamp(boolean relativeTimestamp) {
		this.relativeTimestamp = relativeTimestamp;
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
	public String getRevision() {
		return "";
	}

}
