package de.lmu.ifi.dbs.medmon.medic.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.services.IEntityManagerService;

/**
 * Handles Eclipse Gemini JPA EntityManager services.
 * 
 * @author Nepomuk Seiler
 * @version 0.2
 * 
 */
public class EntityManagerService implements IEntityManagerService {

	private List<EntityManagerFactory>	emFactories	= new ArrayList<EntityManagerFactory>();
	private EntityManagerFactoryBuilder	emfBuilder;

	private final Logger				log			= LoggerFactory.getLogger(IEntityManagerService.class);

	@Override
	public EntityManager createEntityManager() {
		EntityManagerFactory emf = getEntityManagerFactory();
		return emf.createEntityManager();
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		if (emFactories.isEmpty())
			return null;
		// TODO DerbyService -> Get different EntityManagers
		return emFactories.get(0);
	}

	public void bindEntityManagerFactoryBuilder(EntityManagerFactoryBuilder emfBuilder, Map<String, String> properties) {
		this.emfBuilder = emfBuilder;
	}

	public void unbindEntityManagerFactoryBuilder(EntityManagerFactoryBuilder emfBuilder) {
		this.emfBuilder = null;
	}

	public void bindEntityManagerFactory(EntityManagerFactory emf) {
		emFactories.add(emf);
	}

	public void unbindEntityManagerFactory(EntityManagerFactory emf) {
		emFactories.remove(emf);
	}

	protected void activate(ComponentContext context) {
		log.debug("EntityManagerServiceComponent activated");
	}

	protected void deactivate(ComponentContext context) {
	}

}
