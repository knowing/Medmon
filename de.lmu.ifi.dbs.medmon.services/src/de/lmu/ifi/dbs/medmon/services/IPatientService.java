package de.lmu.ifi.dbs.medmon.services;

import java.io.IOException;
import java.net.URI;
import java.util.Date;

import de.lmu.ifi.dbs.medmon.database.entity.Data;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.database.entity.Sensor;
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
	public Data store(Patient p, Sensor s, String type, Date from, Date to);
	
	/**
	 * NOTE: this should return the created {@link Data} entity
	 * 
	 * @param patient
	 * @param type
	 * @param inputURL
	 * @throws IOException
	 */
	public Data store(Patient patient, ISensor sensor, String type, URI inputURL) throws IOException;
	
	
	/**
	 * <p>stores the data located on a sensor into the database</p>
	 * NOTE: this should return the created {@link Data} entity
	 * 
	 * @param patient - the patient to which the data belongs
	 * @param sensorService - a sensor-service
	 * @param type - the type of the data to be stored
	 * @throws IOException 
	 */
	public Data store(Patient patient, ISensor sensorService, String type) throws IOException;
	
	
}
