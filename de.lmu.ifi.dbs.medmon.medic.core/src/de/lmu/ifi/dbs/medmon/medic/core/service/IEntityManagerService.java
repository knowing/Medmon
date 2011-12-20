package de.lmu.ifi.dbs.medmon.medic.core.service;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Therapy;
import de.lmu.ifi.dbs.medmon.database.model.TherapyResult;

public interface IEntityManagerService {
	
	/**
	 * 
	 * @return creates a new EntityManager
	 */
	public EntityManager createEntityManager();
	
	/**
	 * 
	 * @return
	 */
	public EntityManagerFactory getEntityManagerFactory();
}
