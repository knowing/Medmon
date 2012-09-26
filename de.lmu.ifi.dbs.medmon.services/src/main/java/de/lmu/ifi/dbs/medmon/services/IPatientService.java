package de.lmu.ifi.dbs.medmon.services;

import java.io.IOException;

import de.lmu.ifi.dbs.medmon.database.entity.Data;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

/**
 * 
 * <p>
 * This service maps between database and filesystem. The database acts as a cache where addresses to the specific
 * {@link Data} object are stored.
 * </p>
 * 
 * <p>
 * The general structure for a file system could look like this:
 * </p>
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
     * 
     * @param patient
     * @param sensor
     * @param type
     * @param file
     * @return
     * @throws IOException
     */
    public Data store(Patient patient, ISensor sensor, String type, String file) throws IOException;

    /**
     * <p>
     * stores the data located on a sensor into the database
     * </p>
     * NOTE: this should return the created {@link Data} entity
     * 
     * @param patient
     *            - the patient to which the data belongs
     * @param sensorService
     *            - a sensor-service
     * @param type
     *            - the type of the data to be stored
     * @throws IOException
     */
    public Data store(Patient patient, ISensor sensor, String type) throws IOException;

    /**
     * Create a Data.RESULT Data entity
     * 
     * @param patient
     * @param data
     * @return
     * @throws IOException
     */
    public Data store(Patient patient, Data data) throws IOException;

}
