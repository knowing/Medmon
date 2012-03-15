package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.medic.core.service.IEntityManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;

/**
 * Handles Eclipse Gemini JPA EntityManager services.
 * 
 * @author Nepomuk Seiler
 * @version 0.2
 * 
 */
public class EntityManagerService implements IEntityManagerService {

	private List<DataSourceFactory>		dsFactories	= new ArrayList<DataSourceFactory>();
	private List<EntityManagerFactory>	emFactories	= new ArrayList<EntityManagerFactory>();
	private EntityManagerFactoryBuilder	emfBuilder;

	private final Logger				log			= LoggerFactory.getLogger(IPatientService.class);

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

	public void bindDataSourceFactory(DataSourceFactory dsFactory) {
		dsFactories.add(dsFactory);
	}

	public void unbindDataSourceFactory(DataSourceFactory dsFactory) {
		dsFactories.remove(dsFactory);
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

	@Deprecated
	private void dbProperties() {
		Map<String, Object> properties = new HashMap<String, Object>();
		String home = System.getProperty("user.home");
		String sep = System.getProperty("file.separator");
		String dir = home + sep + ".medmon" + sep + "db";
		String url = "jdbc:derby:" + dir + ";create=true";
		properties.put(PersistenceUnitProperties.JDBC_URL, url);
		properties.put(PersistenceUnitProperties.JDBC_DRIVER, "org.apache.derby.jdbc.EmbeddedDriver");
		// emf = emfBuilder.createEntityManagerFactory(properties);
	}

}
