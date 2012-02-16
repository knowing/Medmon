package de.lmu.ifi.dbs.medmon.medic.core.service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * Creates EntityManager for the current db
 * connected.
 * 
 * @author Nepomuk Seiler, Stephan Picker
 * @version 0.2
 * 
 */
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
