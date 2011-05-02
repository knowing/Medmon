/**
 * 
 */
package de.sendsor.accelerationSensor.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

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
public class SDRLoader extends AbstractFileLoader implements IConverter {

	/* ==== Factory ==== */
	public static final String ID = "de.sendsor.accelerationSensor.converter.SDRLoader";
	public static final String URL = "input.url";
	public static final String FILE = "input.file";

	private Properties properties = new Properties();

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

	public SDRLoader() {
		m_structure = ResultsUtil.dateAndValuesResult(Arrays.asList(new String[] { "x", "y", "z" }));
		dataset = new Instances(m_structure);

		timeAttribute = dataset.attribute(ResultsUtil.ATTRIBUTE_TIMESTAMP());
		// initialize value attributes
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
		// super.setSource(input);
	}

	@Override
	public Instances getStructure() throws IOException {
		return m_structure;
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
			/* Reasons for break
			 * 1) record ends when zeros appear
			 * 2) record ends when new date is before the previous entry
			 * 3) given parameter `to` is reached 
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

		// Initialize position handling
		byte[] data = new byte[BLOCKSIZE];

		// Initialize time handling
		Calendar date = new GregorianCalendar();
		Calendar timestamp = new GregorianCalendar();

		// Initialize data handling
		// TODO SDRLoader -> replace RandomAccessFile with FileInputStream?
		RandomAccessFile in = new RandomAccessFile(m_sourceFile, "r");

		// Convert each block
		for (long i = 0; i <= in.length(); i++) {
			// Search position
			long position = i * BLOCKSIZE;
			in.seek(position);
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

			for (int j = 0; j < CONTENT_BLOCK; j += 3) {
				timestamp.setTimeInMillis(time);
				int x = data[j];
				int y = data[j + 1];
				int z = data[j + 2];

				// Avoiding Call-by-Reference effect

				// datalist.add(new Data(timestamp.getTimeInMillis(), x, y, z));
				time += TIME_CORRECTION_AFTER;
				DenseInstance instance = new DenseInstance(4);
				instance.setValue(timeAttribute, timestamp.getTimeInMillis());
				instance.setValue(xAttribute, x);
				instance.setValue(yAttribute, y);
				instance.setValue(z, z);
				dataset.add(instance);
			}

			// Checks if the recorded data ended
			if (recordEnd(day, hour))
				break;

		}
		// log.info("Converted Data[]: " + datalist.size());
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
	 * and will be overitten. This method checks if the end of the recorded data
	 * is reached, however not the end of the file.
	 * 
	 * @param day
	 * @param hour
	 * @return
	 */
	private boolean recordEnd(int day, int hour) {
		return (day == 48) && (hour == 48);
	}

	@Override
	public Instance getNextInstance(Instances structure) throws IOException {
		return null;
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

	/*
	 * @Override public ILoader getInstance(Properties properties) { String
	 * pathname = properties.getProperty(FILE, "/home/muki/input.sdr");
	 * SDRLoader loader = new SDRLoader();
	 * System.out.println("Set loader file"); try { loader.setFile(new
	 * File(pathname)); } catch (IOException e) { e.printStackTrace();
	 * //log.error("Couldn't set file for SDRLoader", e); } return new
	 * SynchronizedLoader(loader); }
	 */

	public static void main(String[] args) {
		SDRLoader loader = new SDRLoader();
		try {
			loader.setFile(new File("/home/muki/input.sdr"));

//			Calendar cal = Calendar.getInstance();
//			cal.set(2010, 6, 7);
//			Date from = (Date) cal.getTime().clone();
//			cal.set(2010, 6, 9);
//			Date to = (Date) cal.getTime().clone();
//			loader.copy(new FileOutputStream("/home/muki/copied.sdr"), from, to);
			IBlock block = loader.convert();
			System.out.println(block);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
