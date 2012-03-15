package de.lmu.ifi.dbs.medmon.medic.core.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Date;

import de.lmu.ifi.dbs.medmon.database.entity.Data;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.database.entity.Sensor;
import de.lmu.ifi.dbs.medmon.medic.core.util.DataStoreOutput;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

/**
 * 
 * <p>This service maps between database and filesystem. 
 * The database acts as a cache where addresses to the
 * specific {@link Data} object are stored.</p>
 * 
 * <p>The general structure for a file system could
 * look like this:</p>
 * <p>
 * ROOT<br>
 * - RESULT<br>
 * - RAW<br>
 * - TRAIN<br>
 * </p>
 * 
 * @author Nepomuk Seiler
 * @version 1.1
 * @since 2011-11-06
 *
 */
public interface IPatientService {

	public static String	TRAIN	= "train";
	public static String	RESULT	= "result";
	public static String	RAW		= "raw";
	public static String	ROOT	= "root";
	
	/**
	 * 
	 * @param p - Which Patient directory
	 * @param type - TRAIN, RESULT, RAW or ROOT
	 * @return absolute path
	 */
	public Path locateDirectory(Patient p, String type);
	
	/**
	 * 
	 * @param d - Data instance from db
	 * @return absolute path to file
	 */
	public Path locateFile(Data d);
	
	/**
	 * <p>Returns relative path to the Data instance, e.g <p>
	 * locateFilename(d, ROOT) -> "train/00001.sdr" <br>
	 * locateFilename(d, TRAIN) -> "00001.sdr" </p>
	 * </p>
	 * 
	 * @param d - Data instance from db
	 * @param relativeToType - TRAIN, RESULT, RAW or ROOT
	 * @return relative Path to relativeToType param
	 */
	public Path locateFilename(Data d, String relativeToType);

	
	/**
	 * Creates the File Hierarchy, that is used to store Data for the Patient
	 * 
	 * @param p - Patient to be initialized
	 */
	public void initializePatient(Patient p) throws IOException;
	
	
	/**
	 * Deletes all Content of a Patient that has been created by initializePatient()
	 * 
	 * @param p - Patient to be released
	 * @throws IOException 
	 */
	public void releasePatient(Patient p) throws IOException;
	
	/**
	 * <p>
	 * Generates an OutputStream based on the given parameters.
	 * </p>
	 * 
	 * <p>NOTE: This method should return the OutputStream and the {@link Data} object
	 * it has created. User can use the data entity more easy after storing it.</p>
	 * 
	 * @param p - Patient associated with data
	 * @param s - Sensor associated with data
	 * @param type - data type - RAW, TRAIN or RESULT
	 * @param from - Data recording start
	 * @param to - Data recording end
	 * @return - Ready to write OutputStream
	 * @throws IOException 
	 */
	public DataStoreOutput store(Patient p, Sensor s, String type, Date from, Date to) throws IOException;
	
	/**
	 * NOTE: this should return the created {@link Data} entity
	 * 
	 * @param patient
	 * @param sensor
	 * @param type
	 * @param inputURL
	 * @throws IOException
	 */
	public DataStoreOutput store(Patient patient, ISensor sensor, String type, URI inputURL) throws IOException;
	
	
	/**
	 * <p>stores the data located on a sensor into the database</p>
	 * NOTE: this should return the created {@link Data} entity
	 * 
	 * @param patient - the patient to which the data belongs
	 * @param sensorService - a sensor-service
	 * @param type - the type of the data to be stored
	 * @throws IOException 
	 */
	public DataStoreOutput store(Patient patient, ISensor sensorService, String type) throws IOException;
	
	
	/**
	 * <p>Generates an InputStream to load the
	 * database from the underlying source</p>
	 * 
	 * @param d - Data from db
	 * @return - Ready to read InputStream
	 * @throws IOException
	 */
	public InputStream load(Data d) throws IOException;
	
	/**
	 * <p> Generates an OutputStream based on the information
	 * of d1 and d2. <br>
	 * Note: Doesn't delete d1 and d2. This must be done
	 * with {@code remove(Data d)}
	 * </p>
	 * 
	 * @param d1
	 * @param d2
	 * @return Ready to write OutputStream
	 * @throws IOException
	 */
	public OutputStream merge(Data d1, Data d2) throws IOException;
}
