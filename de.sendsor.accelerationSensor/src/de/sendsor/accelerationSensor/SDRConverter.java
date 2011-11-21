package de.sendsor.accelerationSensor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.joda.time.Interval;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import de.lmu.ifi.dbs.medmon.sensor.core.FileConverter;
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
public class SDRConverter extends FileConverter {

	private static final long serialVersionUID = 7663052852394853876L;

	/* ==== Factory ==== */
	public static final String ID = SDRLoader.class.getName();
	public static final String URL = "url";
	public static final String FILE = "file";

	/* ==== Loader ==== */
	private Instances header;

	public static String FILE_EXTENSION = ".sdr";

	private final static int BLOCKSIZE = 512;
	private final static int CONTENT_BLOCK = 504;

	/** 168 instances / 25Hz = 6720 ms */
	private final static long TIME_CORRECTION_BEFORE = 6720;
	/** 1 / 25Hz = 40ms */
	private final static long SAMPLE_DISTANCE = 40;

	private Attribute timeAttribute;
	private Attribute xAttribute;
	private Attribute yAttribute;
	private Attribute zAttribute;

	/* == OPTIONS == */
	private String interval = SDRLoaderFactory.INTERVAL_SECOND();
	private String aggregate = SDRLoaderFactory.AGGREGATE_AVERAGE();
	private double units = 1.0;
	private boolean relativeTimestamp = false;
	private String output = null;

	/* */
	private boolean firstRun = true;

	public SDRConverter(URL url) throws IOException {
		super(url);
		init();
	}

	public SDRConverter(String file) throws IOException {
		super(file);
		init();
	}
	
	public SDRConverter(InputStream input) {
		super(input);
		init();
	}

	private void init() {
		header = ResultsUtil.timeSeriesResult(Arrays.asList(new String[] { "x", "y", "z" }));

		timeAttribute = header.attribute(ResultsUtil.ATTRIBUTE_TIMESTAMP());
		// initialize value attributes
		// TODO SDRConverter -> use ResultUtils.findValueAttributesAsJavaMap
		List<Attribute> valueAttributes = ResultsUtil.findValueAttributes(header);
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
	public Instances getHeader() {
		return header;
	}

	@Override
	public Instances getData() throws IOException {
		Instances dataset = new Instances(header);
		if (input == null)
			throw new IOException("No source defined - Null InputStream");

		// Initialize position handling
		byte[] data = new byte[BLOCKSIZE];

		// Initialize time handling
		Calendar date = new GregorianCalendar();

		Calendar intervalstart = new GregorianCalendar();
		Calendar intervalcurrent = new GregorianCalendar();
		// Initialize data handling

		int read = input.read(data);

		int avg_x = 0;
		int avg_y = 0;
		int avg_z = 0;
		boolean newInterval = true;
		long interval = getIntervalLength();
		if (units > 0)
			interval *= units;

		// Convert each block
		while (read != -1) {
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
				// Increase time interval
				boolean insideBounds = intervalcurrent.getTimeInMillis() - intervalstart.getTimeInMillis() < interval;
				if (aggregate.equals("none")) {
					// Don't aggregate, just take raw values
					DenseInstance instance = new DenseInstance(4);
					instance.setValue(timeAttribute, time);
					instance.setValue(xAttribute, x);
					instance.setValue(yAttribute, y);
					instance.setValue(zAttribute, z);
					dataset.add(instance);
					intervalstart.setTimeInMillis(time);
					intervalcurrent.setTimeInMillis(time);
				} else if (!insideBounds) {
					// New interval begins, save old one
					DenseInstance instance = new DenseInstance(4);
					instance.setValue(timeAttribute, time);
					instance.setValue(xAttribute, avg_x);
					instance.setValue(yAttribute, avg_y);
					instance.setValue(zAttribute, avg_z);
					dataset.add(instance);
					// Start for new aggregation
					// if aggregation < SAMPLE_DISTANCE first sample will be
					// used twice!
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
				time += SAMPLE_DISTANCE;

			}
			date.setTimeInMillis(time);
			// Load Data into data-Buffer
			read = input.read(data);

		}
		// Saves to output if option set
		if (output != null) {
			File out = new File(output);
			ArffSaver arffSaver = new ArffSaver();
			arffSaver.setFile(out);
			arffSaver.setInstances(dataset);
			arffSaver.writeBatch();
		}

		input.close();
		return dataset;
	}

	@Override
	public Interval getInterval() throws IOException {
		// Initialize time handling
		Calendar current = new GregorianCalendar();

		// Initialize position handling
		byte[] data = new byte[BLOCKSIZE];
		int read = input.read(data);
		
		// Initialize date or return zero Interval
		if(setTime(current, data)) 
			return new Interval(0, 0);
		
		long start = current.getTimeInMillis();
		
		// Convert each block
		while (read != -1) {
			// Create timestamp
			boolean recordEnd = setTime(current, data);

			// Checks if the recorded data ended
			if (recordEnd)
				break;

			// Load Data into data-Buffer
			read = input.read(data);
		}
		
		input.close();
		return new Interval(start, current.getTimeInMillis());
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
//			System.out.println("relative timestamp: " + calendar.getTime());
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

	public void setOutput(String output) {
		this.output = output;
	}

}
