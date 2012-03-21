package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.walkFileTree;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.READ;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.joda.time.Interval;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.database.entity.Data;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.database.entity.Sensor;
import de.lmu.ifi.dbs.medmon.medic.core.service.IEntityManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;
import de.lmu.ifi.dbs.medmon.medic.core.service.ISensorManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.util.DataStoreOutput;
import de.lmu.ifi.dbs.medmon.medic.core.util.DeleteDirectoryVisitor;
import de.lmu.ifi.dbs.medmon.sensor.core.IConverter;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

public class PatientService implements IPatientService {

	private final Logger			log						= LoggerFactory.getLogger(IPatientService.class);

	/** Format patient-id to 12 digits */
	private final DecimalFormat		decimalF				= new DecimalFormat("000000000000");

	/** Format dates with DateFormat.SHORT */
	private final DateFormat		dateF					= new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss-SSS");

	private IEntityManagerService	entityManagerService	= null;
	private ISensorManagerService	sensorManagerService	= null;

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
		IConverter converter = sensorManagerService.createConverter(s);

		if (converter == null)
			throw new IOException("No converter found for sensor " + s.getName());

		Sensor entity = sensorManagerService.loadSensorEntity(s);
		Interval interval = converter.getInterval();

		Data data = store(p, entity, type, interval.getStart().toDate(), interval.getEnd().toDate());

		try (InputStream in = sensorManagerService.createDefaultInput(s)) {
			Files.copy(in, data.toPath());
			return data;
		} catch (IOException e) {
			// Log error and forward to caller
			log.error("Error read from InputStream or writing to OutputStream.", e);
			throw e;
		}
	}

	/**
	 * 
	 * @param patient
	 * @param sensor
	 * @param type
	 * @param inputURI
	 */
	@Override
	public Data store(Patient patient, ISensor sensor, String type, URI inputURI) throws IOException {
		InputStream inputStream = sensorManagerService.createInput(sensor, inputURI);
		IConverter converter = sensor.newConverter(inputStream);

		if (converter == null)
			throw new IOException("No converter found for sensor " + sensor.getName());

		Sensor entity = sensorManagerService.loadSensorEntity(sensor);
		Interval interval = converter.getInterval();

		Data data = store(patient, entity, type, interval.getStart().toDate(), interval.getEnd().toDate());
		// Copy raw data
		try (OutputStream os = Files.newOutputStream(data.toPath())) {
			Files.copy(Paths.get(inputURI), os);
			return data;
		} catch (IOException e) {
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
