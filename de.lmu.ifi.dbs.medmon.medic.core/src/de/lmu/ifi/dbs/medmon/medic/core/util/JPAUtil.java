package de.lmu.ifi.dbs.medmon.medic.core.util;

import javax.persistence.EntityManager;

import de.lmu.ifi.dbs.medmon.services.IEntityManagerService;

import static de.lmu.ifi.dbs.medmon.medic.core.Activator.getEntityManagerService;

public class JPAUtil {

	/**
	 * @return a new entityManager
	 */
	public static EntityManager createEntityManager() {
		IEntityManagerService ems = getEntityManagerService();
		if (ems == null)
			return null;
		return ems.createEntityManager();
	}
}
