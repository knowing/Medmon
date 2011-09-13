package de.lmu.ifi.dbs.medmon.medic.core.service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public interface IDerbyService {

	/**
	 * @return always the same EntityManager instances
	 */
	EntityManager getEntityManager();
	
	/**
	 * 
	 * @return creates a new EntityManager
	 */
	EntityManager createEntityManager();
	
	/**
	 * 
	 * @return
	 */
	EntityManagerFactory getEntityManagerFactory();
}
