package de.lmu.ifi.dbs.medmon.medic.core.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
import de.lmu.ifi.dbs.medmon.sensor.core.ISensorManager;
import de.lmu.ifi.dbs.medmon.services.IEntityManagerService;
import de.lmu.ifi.dbs.medmon.services.IPatientService;

public class PatientService implements IPatientService {

	private final Logger			log						= LoggerFactory.getLogger(IPatientService.class);

	/** Format patient-id to 12 digits */
	private final DecimalFormat		decimalF				= new DecimalFormat("000000000000");

	/** Format dates with DateFormat.SHORT */
	private final DateFormat		dateF					= new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss-SSS");

	private IEntityManagerService	entityManagerService	= null;
	private ISensorManager	sensorManagerService	= null;

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
		EntityManager tempEM = entityManagerService.createEntityManager();
		tempEM.getTransaction().begin();
		Data data = new Data(from, to, s);
		data.setPatient(p);
		data.setType(type);
		tempEM.persist(data);
		tempEM.getTransaction().commit();
		tempEM.close();

		return data;
	}

	/**
	 * 
	 */
	@Override
	public Data store(Patient p, ISensor s, String type) throws IOException {

		if (s.getDriver() == null)
			throw new IOException("No driver found for sensor " + s.getName());

		Sensor entity = sensorManagerService.loadSensorEntity(s);
		Interval interval = s.getInterval();

		Data data = store(p, entity, type, interval.getStart().toDate(), interval.getEnd().toDate());
		
		try (InputStream in = Files.newInputStream(Paths.get(sensorManagerService.createDefaultURI(s)))) {
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

	protected void bindSensorManagerService(ISensorManagerService service) {
		sensorManagerService = service;
	}

	protected void unbindSensorManagerService(ISensorManagerService service) {
		sensorManagerService = null;
	}

}
