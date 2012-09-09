package de.lmu.ifi.dbs.medmon.medic.core.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Date;

import javax.persistence.EntityManager;

import org.joda.time.Interval;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.database.entity.Data;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.database.entity.Sensor;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;
import de.lmu.ifi.dbs.medmon.services.IEntityManagerService;
import de.lmu.ifi.dbs.medmon.services.IPatientService;

public class PatientService implements IPatientService {

	private final Logger			log						= LoggerFactory.getLogger(IPatientService.class);

	private IEntityManagerService	entityManagerService	= null;

	/**
	 * <p>
	 * Creates a new file and {@link Data} instance and returns the
	 * corresponding Data entity.
	 * </p>
	 * 
	 * @param p
	 * @param s
	 * @param type
	 * @param from
	 * @param to
	 * @return Ready to write OutputStream
	 */
	@Override
	public Data store(Patient p, Sensor s, String type, Date from, Date to) {
		EntityManager em = entityManagerService.createEntityManager();
		em.getTransaction().begin();
		Data data = new Data(from, to, s);
		data.setPatient(p);
		data.setType(type);
		em.persist(data);
		em.getTransaction().commit();
		em.close();

		return data;
	}

	/**
	 * 
	 */
	@Override
	public Data store(Patient p, ISensor sensor, String type) throws IOException {

		if (sensor.getDriver() == null)
			throw new IOException("No driver found for sensor " + sensor.getName());

		Interval interval = sensor.getInterval();

		// Data data = store(p, entity, type, interval.getStart().toDate(),
		// interval.getEnd().toDate());
		EntityManager em = entityManagerService.createEntityManager();
		em.getTransaction().begin();
		// TODO load sensor entity - test
		Sensor entity = em.find(Sensor.class, sensor.getId());
		Data data = new Data(interval.getStart().toDate(), interval.getEnd().toDate(), entity);
		data.setPatient(p);
		data.setType(type);
		em.persist(data);
		em.getTransaction().commit();
		em.close();

		// Store file
		try (InputStream in = sensor.getDataInputStream()) {
			Files.copy(in, data.toPath());
			return data;
		} catch (IOException e) {
			// Log error and forward to caller
			log.error("Error read from InputStream or writing to OutputStream.", e);
			throw e;
		}
	}

	protected void activate(ComponentContext context) {
		log.debug("PatientService activated. Properties: " + context.getProperties());
	}

	protected void bindEntityManager(IEntityManagerService service) {
		entityManagerService = service;
	}

	protected void unbindEntityManager(IEntityManagerService service) {
		entityManagerService = null;
	}

}
