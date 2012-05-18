package de.sendsor.accelerationSensor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.MutableDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import de.lmu.ifi.dbs.knowing.core.results.TimeSeriesResults;
import de.lmu.ifi.dbs.medmon.sensor.core.FileConverter;

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

	private static final long	serialVersionUID		= 7663052852394853876L;
	private final Logger		log						= LoggerFactory.getLogger(SDRConverter.class);

	/* ==== Factory ==== */
	public static final String	ID						= SDRLoader.class.getName();
	public static final String	URL						= "url";
	public static final String	FILE					= "file";

	/* ==== Loader ==== */
	private Instances			header;

	public static String		FILE_EXTENSION			= ".sdr";

	private final static int	BLOCKSIZE				= 512;
	private final static int	CONTENT_BLOCK			= 504;

	/** 168 instances / 25Hz = 6720 ms */
	private final static long	TIME_CORRECTION_BEFORE	= 6720;
	/** 1 / 25Hz = 40ms */
	private final static long	SAMPLE_DISTANCE			= 40;

	private Attribute			timeAttribute;
	private Attribute			xAttribute;
	private Attribute			yAttribute;
	private Attribute			zAttribute;

	/* == OPTIONS == */
	private String				interval				= SDRLoaderFactory.INTERVAL_SECOND();
	private String				aggregate				= SDRLoaderFactory.AGGREGATE_AVERAGE();
	private double				units					= 1.0;
	private boolean				relativeTimestamp		= false;

	public SDRConverter(URI url) throws IOException {
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
		header = TimeSeriesResults.newInstances(Arrays.asList(new String[] { "x", "y", "z" }));

		timeAttribute = header.attribute(TimeSeriesResults.ATTRIBUTE_TIMESTAMP());
		// initialize value attributes
		// TODO SDRConverter -> use ResultUtils.findValueAttributesAsJavaMap
		List<Attribute> valueAttributes = TimeSeriesResults.findValueAttributes(header);
		for (Attribute attribute : valueAttributes) {
			String name = attribute.getMetadata().getProperty(TimeSeriesResults.META_ATTRIBUTE_NAME());
			if (name.equals("x"))
				xAttribute = attribute;
			else if (name.equals("y"))
				yAttribute = attribute;
			else if (name.equals("z"))
				zAttribute = attribute;
		}
	}

	@Override
	public Instances getData() throws IOException {
		Instances dataset = new Instances(header);
		if (input == null)
			throw new IOException("No source defined - Null InputStream");

		// Initialize position handling
		byte[] data = new byte[BLOCKSIZE];

		// Initialize data handling
		int read = input.read(data);

		int avg_x = 0;
		int avg_y = 0;
		int avg_z = 0;

		// Initialize time handling
		DateTime current = nextDateTimeAbsolute(data);
		MutableDateTime nextRelative = current.toMutableDateTime();

		Duration duration = new Duration(getDurationLength());
		Interval interval = new Interval(current, duration);
		boolean newInterval = true;

		// Convert each block
		while (read != -1) {

			// Load Data into data-Buffer
			read = input.read(data);

			// Create timestamp
			DateTime nextAbsolute = nextDateTimeAbsolute(data);
			nextDateTimeRelative(nextRelative);

			if (recordEnd(current, nextAbsolute)) {
				log.info("END OF FILE REACHED. OLD DATA BEGINS: " + nextAbsolute);
				break;
			}

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
				}
				// Increase time interval
				boolean insideBounds = isInsideBounds(interval, nextAbsolute, nextRelative);
				if (aggregate.equals("none")) {
					// Don't aggregate, just take raw values
					DenseInstance instance = new DenseInstance(4);
					instance.setValue(timeAttribute, nextDateTimeValue(nextAbsolute, nextRelative));
					instance.setValue(xAttribute, x);
					instance.setValue(yAttribute, y);
					instance.setValue(zAttribute, z);
					dataset.add(instance);
				} else if (!insideBounds) {
					// New interval begins, save old one
					DenseInstance instance = new DenseInstance(4);
					instance.setValue(timeAttribute, nextDateTimeValue(nextAbsolute, nextRelative));
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
					interval = nextInterval(duration, nextAbsolute, nextRelative);
				} else if (insideBounds) {
					// Still in our time interval bounds
					avg_x = (avg_x + x) / 2;
					avg_y = (avg_y + y) / 2;
					avg_z = (avg_z + z) / 2;
				}
				nextRelative.add(SAMPLE_DISTANCE);
			}

			current = nextAbsolute;
		}

		input.close();
		return dataset;
	}

	@Override
	public Interval getInterval() throws IOException {
		// Initialize position handling
		byte[] data = new byte[BLOCKSIZE];
		int read = input.read(data);

		DateTime start = nextDateTimeAbsolute(data);
		DateTime current = nextDateTimeAbsolute(data);

		if (start.getMillis() == 0)
			throw new IOException("SDR file doesn't contain any data on InputStream [" + input + "]");

		// Convert each block
		while (read != -1) {
			// Load Data into data-Buffer
			read = input.read(data);

			// Set timestamp
			DateTime next = nextDateTimeAbsolute(data);

			boolean recordEnd = recordEnd(current, next);

			// Checks if the recorded data ended
			if (recordEnd)
				break;

			current = next;

		}

		input.close();
		return new Interval(start, current);
	}

	/**
	 * <h4>RelativeTimestamp handling</h4>
	 * <p>
	 * If option relativeTimestamp is set, this method does nothing except on
	 * the<br>
	 * first run and initially sets the timestamp.
	 * </p>
	 * 
	 * <h4>Checks for file end based on read day/hour field</h4>
	 * <p>
	 * Currently a SDR file is a bunch of zeros. Those zeros are placeholders<br>
	 * and will be overwritten. This method checks if the end of the recorded<br>
	 * data is reached, however not the end of the file.
	 * </p>
	 * 
	 * @param current
	 *            - if null -> first run
	 * @param data
	 *            - 512 byte array
	 * @return new DateTime instance
	 */
	private DateTime nextDateTimeAbsolute(byte[] data) {
		int year = calcYear(data[506]);
		int month = data[507] - 1;
		int day = data[508];
		int hour = data[509];
		int minute = data[510];
		int second = data[511];

		// Return DateTime(0) to trigger recordEnd method
		if (day == 48 && hour == 48)
			return new DateTime(0);
		if(0 > hour || hour > 24  || 1 > day ||  day > 31 || 1 > month || month > 12 )
			return new DateTime(0);

		return new DateTime(year, month, day, hour, minute, second).minus(TIME_CORRECTION_BEFORE);
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
	 * 
	 * @param current
	 * @param next
	 * @return next.isBefore(current)
	 */
	private boolean recordEnd(DateTime current, DateTime next) {
		return next.isBefore(current);
	}

	/**
	 * 
	 * @param current
	 * @return
	 */
	private void nextDateTimeRelative(MutableDateTime current) {
		current.add(SAMPLE_DISTANCE);
	}

	private boolean isInsideBounds(Interval interval, DateTime nextAbsolute, MutableDateTime nextRelative) {
		if (relativeTimestamp)
			return interval.contains(nextRelative);
		return interval.contains(nextAbsolute);
	}

	private Interval nextInterval(Duration duration, DateTime nextAbsolute, MutableDateTime nextRelative) {
		if (relativeTimestamp)
			return new Interval(nextRelative, duration);
		return new Interval(nextAbsolute, duration);
	}

	private long nextDateTimeValue(DateTime nextAbsolute, MutableDateTime nextRelative) {
		if (relativeTimestamp)
			return nextRelative.getMillis();
		return nextAbsolute.getMillis();
	}

	/**
	 * 
	 * @return TimeUnit.<interval> * units
	 */
	private long getDurationLength() {
		double units = Math.abs(this.units);
		if (interval.equals(SDRLoaderFactory.INTERVAL_SECOND()))
			return (long) (TimeUnit.SECONDS.toMillis(1) * units);
		else if (interval.equals(SDRLoaderFactory.INTERVAL_MINUTE()))
			return (long) (TimeUnit.MINUTES.toMillis(1) * units);
		else if (interval.equals(SDRLoaderFactory.INTERVAL_HOUR()))
			return (long) (TimeUnit.HOURS.toMillis(1) * units);
		else if (interval.equals(SDRLoaderFactory.INTERVAL_DAY()))
			return (long) (TimeUnit.DAYS.toMillis(1) * units);
		else
			return (long) (TimeUnit.SECONDS.toMillis(1) * units);
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

}
