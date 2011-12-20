package de.lmu.ifi.dbs.medmon.medic.core.service;

import java.io.IOException;

import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Therapy;
import de.lmu.ifi.dbs.medmon.database.model.TherapyResult;

public interface IDBModelService {
	
	/**
	 * <p>This has to be called when a {@link Patient} is
	 * deleted in the database. This will delete ALL data
	 * linked with the patient.</p>
	 * 
	 * @param p - patient to delete
	 * @throws IOException
	 */
	public void deletePatient(Patient p) throws IOException;
	
	/**
	 * <p>Throws an exception if the corresponding source
	 * couldn't be deleted</p>
	 * @param d - Data instance to delete
	 * @throws IOException
	 */
	public void deleteData(Data d) throws IOException;
	
	/**
	 * <p>Throws an exception if the corresponding source
	 * couldn't be deleted</p>
	 * @param t - TherapyResult instance to delete
	 * @throws IOException
	 */
	public void deleteTherapyResult(TherapyResult r) throws IOException;
	
	/**
	 * <p>Throws an exception if the corresponding source
	 * couldn't be deleted</p>
	 * @param t - Therapy instance to delete
	 * @throws IOException
	 */
	public void deleteTherapy(Therapy t) throws IOException;

	/**
	 * <p>This has to be called when a new {@link Patient} is
	 * persisted in the database.</p>
	 * 
	 * @return newly created Patient
	 * @throws IOException
	 */
	public Patient createPatient() throws IOException;
}
