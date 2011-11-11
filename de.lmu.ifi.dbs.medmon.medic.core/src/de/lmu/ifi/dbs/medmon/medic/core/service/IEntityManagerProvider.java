package de.lmu.ifi.dbs.medmon.medic.core.service;

import javax.persistence.EntityManager;

public interface IEntityManagerProvider {

	/**
	 * unregisters this service
	 */
	public void unregister();
	/**
	 * @return always the same EntityManager instances
	 */
	public EntityManager getEntityManager();
	
	/**
	 * called by service
	 */
	public void setEntityManagerService(IEntityManagerService serivce);
}
