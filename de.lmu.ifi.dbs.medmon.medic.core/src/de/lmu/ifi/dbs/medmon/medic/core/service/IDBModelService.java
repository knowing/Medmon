package de.lmu.ifi.dbs.medmon.medic.core.service;

import java.io.IOException;
import java.util.Date;

import de.lmu.ifi.dbs.medmon.database.entity.Data;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.database.entity.Sensor;
import de.lmu.ifi.dbs.medmon.database.entity.Therapy;
import de.lmu.ifi.dbs.medmon.database.entity.TherapyResult;

public interface IDBModelService {

	/**
	 * <p>
	 * This has to be called when a {@link Patient} is deleted in the database.
	 * This will delete ALL data linked with the patient.
	 * </p>
	 * 
	 * @param p
	 *            - patient to delete
	 * @throws IOException
	 */
	public void deletePatient(Patient p) throws IOException;

	/**
	 * <p>
	 * Throws an exception if the corresponding source couldn't be deleted
	 * </p>
	 * 
	 * @param d
	 *            - Data instance to delete
	 * @throws IOException
	 */
	public void deleteData(Data d) throws IOException;

	/**
	 * <p>
	 * Throws an exception if the corresponding source couldn't be deleted
	 * </p>
	 * 
	 * @param t
	 *            - TherapyResult instance to delete
	 * @throws IOException
	 */
	public void deleteTherapyResult(TherapyResult r) throws IOException;

	/**
	 * <p>
	 * Throws an exception if the corresponding source couldn't be deleted
	 * </p>
	 * 
	 * @param t
	 *            - Therapy instance to delete
	 * @throws IOException
	 */
	public void deleteTherapy(Therapy t) throws IOException;

	/**
	 * Create a new Patient entity
	 * 
	 * @return newly created Patient
	 * @throws IOException
	 */
	public Patient createPatient() throws IOException;

	/**
	 * Creates a new Therapy for a Patient
	 * 
	 * @param p
	 *            - a Patient
	 * @return a new Therapy
	 */
	public Therapy createTherapy(Patient p);

	/**
	 * Creates a new TherapyResult entity and connects it to a Data entity and a
	 * Therapy entity
	 * 
	 * @param d
	 *            - a Data entity
	 * @param t
	 *            - a Therapy entity
	 * @return - a new TherapyResult
	 */
	public TherapyResult createTherapyResult(Data d, Therapy t);

	/**
	 * Create a mew Data entity
	 * 
	 * @param patient
	 * @param sensor
	 * @param type
	 * @param from
	 * @param to
	 * @return
	 */
	public Data createData(Patient p, Sensor s, String type, Date from, Date to, String file);

}
