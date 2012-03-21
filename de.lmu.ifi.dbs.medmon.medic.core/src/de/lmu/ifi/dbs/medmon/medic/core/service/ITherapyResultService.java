package de.lmu.ifi.dbs.medmon.medic.core.service;

import java.net.URI;

import de.lmu.ifi.dbs.knowing.core.model.IDataProcessingUnit;
import de.lmu.ifi.dbs.medmon.database.entity.Data;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.database.entity.Therapy;
import de.lmu.ifi.dbs.medmon.database.entity.TherapyResult;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

/**
 * 
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 26.12.2011
 */
public interface ITherapyResultService {

	/**
	 * Creates a new {@link Data} entity which is marked as result from the
	 * parameter data.
	 * 
	 * @param dpu - to execute
	 * @param patient - associated patient
	 * @param therapy - associated therapy
	 * @param data- RAW entity
	 * @return {@link TherapyResult} entity, detached
	 */
	TherapyResult createTherapyResult(IDataProcessingUnit dpu, Patient patient, Therapy therapy,Data data) throws Exception;

	/**
	 * Creates a new {@link Data} entity which is marked as result from the
	 * parameter sensor and input.
	 * 
	 * @param dpu - to execute
	 * @param patient associated patient
	 * @param therapy - associated therapy
	 * @param sensor
	 * @param input
	 * @return {@link TherapyResult} entity, detached
	 */
	TherapyResult createTherapyResult(IDataProcessingUnit dpu, Patient patient,Therapy therapy, ISensor sensor, URI input) throws Exception;
}
