package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.walkFileTree;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.READ;

import java.io.File;
import java.io.FileInputStream;
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

import org.joda.time.Interval;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.medic.core.Activator;
import de.lmu.ifi.dbs.medmon.medic.core.service.IEntityManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;
import de.lmu.ifi.dbs.medmon.medic.core.service.ISensorManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.util.DataStoreOutput;
import de.lmu.ifi.dbs.medmon.medic.core.util.DeleteDirectoryVisitor;
import de.lmu.ifi.dbs.medmon.sensor.core.IConverter;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

public class PatientService implements IPatientService {

	private final Logger			log						= LoggerFactory.getLogger(IPatientService.class);

	/** Static application directory in {user.home}/.medmon */
	private final Path				medmon					= Paths.get(System.getProperty("user.home"), ".medmon", "patients");

	/** Format patient-id to 12 digits */
	private final DecimalFormat		decimalF				= new DecimalFormat("000000000000");

	/** Format dates with DateFormat.SHORT */
	private final DateFormat		dateF					= new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss-SSS");

	private IEntityManagerService	entityManagerService	= null;
	private ISensorManagerService	sensorManagerService	= null;

	@Override
	public Path locateDirectory(Patient p, String type) {
		switch (type) {
		case ROOT:
			return medmon.resolve(Paths.get(decimalF.format(p.getId())));
		default:
			return medmon.resolve(Paths.get(decimalF.format(p.getId()), type));
		}
	}

	@Override
	public Path locateFile(Data d) {
		EntityManager em = entityManagerService.createEntityManager();
		Data data = em.merge(d);
		String file = generateFilename(data.getSensor(), data.getType(), data.getFrom(), data.getTo());
		Path ret = locateDirectory(data.getPatient(), data.getType()).resolve(file);
		em.close();
		return ret;
	}

	@Override
	public Path locateFilename(Data d, String relativeToType) {
		Path path = locateFile(d);
		switch (relativeToType) {
		case ROOT:
			// Climb two steps in the hierarchy to be in the ROOT folder
			return path.getParent().getParent().relativize(path);
		default:
			return path.getFileName();
		}
	}

	@Override
	public void initializePatient(Patient p) throws IOException {

		Path root = createDirectories(locateDirectory(p, ROOT));
		createDirectory(root.resolve(TRAIN));
		createDirectory(root.resolve(RESULT));
		createDirectory(root.resolve(RAW));
	}

	@Override
	public void releasePatient(Patient p) throws IOException {

		walkFileTree(locateDirectory(p, ROOT), new DeleteDirectoryVisitor());
	}

	/**
	 * <p>
	 * Creates a new file and {@link Data} instance and returns the
	 * corresponding OutputStream.
	 * </p>
	 * 
	 * @param p
	 * @param s
	 * @param type
	 * @param from
	 * @param to
	 * @return Ready to write OutputStream
	 * @throws IOException
	 */
	@Override
	public DataStoreOutput store(Patient p, Sensor s, String type, Date from, Date to) throws IOException {

		Path file = locateDirectory(p, type).resolve(generateFilename(s, type, from, to));

		Data data = Activator.getDBModelService().createData(p, s, type, from, to, file.toString());

		OutputStream outputStream = null;
		try {
			outputStream = newOutputStream(file, CREATE_NEW);
		} catch (IOException e) {
			Activator.getDBModelService().deleteData(data);
			throw e;
		}

		return new DataStoreOutput(outputStream, data);
	}

	/**
	 * 
	 */
	@Override
	public DataStoreOutput store(Patient p, ISensor s, String type) throws IOException {
		IConverter converter = sensorManagerService.createConverter(s);

		if (converter == null)
			return null;

		Sensor entity = sensorManagerService.loadSensorEntity(s);
		Interval interval = converter.getInterval();

		try (DataStoreOutput output = store(p, entity, type, interval.getStart().toDate(), interval.getEnd().toDate());
				OutputStream os = output.outputStream;
				InputStream in = sensorManagerService.createDefaultInput(s)) {

			byte[] buffer = new byte[4096];
			int bytesRead = 0;
			while ((bytesRead = in.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			return output;
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
	public DataStoreOutput store(Patient patient, ISensor sensor, String type, URI inputURI) throws IOException {
		InputStream inputStream = sensorManagerService.createInput(sensor, inputURI);
		IConverter converter = sensor.newConverter(inputStream);

		if (converter == null)
			return null;

		Sensor entity = sensorManagerService.loadSensorEntity(sensor);
		Interval interval = converter.getInterval();

		//Copy raw data
		try (DataStoreOutput output = store(patient, entity, type, interval.getStart().toDate(), interval.getEnd().toDate());
				OutputStream os = output.outputStream) {

			Files.copy(Paths.get(inputURI), os);
			return output;
		} catch (IOException e) {
			log.error("Error read from InputStream or writing to OutputStream.", e);
			throw e;
		}
	}

	/**
	 * <p>
	 * Locates the corresponding file
	 * </p>
	 * 
	 * @param d
	 *            - detached {@link Data} object
	 * @return Ready to write InputStream
	 * @throws IOException
	 */
	@Override
	public InputStream load(Data d) throws IOException {
		return newInputStream(locateFile(d), READ);
	}

	@Override
	public OutputStream merge(Data d1, Data d2) throws IOException {
		if (!d1.getType().equals(d2.getType()))
			throw new IOException("Data.type doesn't match: " + d1.getType() + " != " + d2.getType());

		Date from, to = null;
		if (d1.getFrom().before(d2.getFrom()))
			from = d1.getFrom();
		else
			from = d2.getFrom();
		if (d1.getTo().after(d2.getTo()))
			to = d1.getTo();
		else
			to = d2.getTo();

		return store(d1.getPatient(), d1.getSensor(), d1.getType(), from, to).outputStream;
	}

	/**
	 * <p>
	 * Generates a filename with the given parameters like this
	 * </p>
	 * 
	 * <p>
	 * <li>RAW: [from]_to_[to].[sensor.id].[sensor.filePrefix]</li>
	 * <li>TRAIN: [from]_to_[to].arff</li>
	 * <li>RESULT: [from]_to_[to].arff</li>
	 * </p>
	 * <p>
	 * For date formatting {@link DateFormat.MEDIUM} is used.
	 * </p>
	 * 
	 * @param s - Sensor
	 * @param type - RAW, TRAIN or RESULT
	 * @param from - Data recording start
	 * @param to - Data recording end
	 * @return
	 */
	private String generateFilename(Sensor s, String type, Date from, Date to) {
		StringBuilder sb = new StringBuilder(32);
		sb.append(dateF.format(from)).append("_to_").append(dateF.format(to)).append(".");
		switch (type) {
		case RAW:
			return sb.append(s.getId()).append(".").append(s.getFilePrefix()).toString();
		case TRAIN:
			return sb.append("arff").toString();
		case RESULT:
			return sb.append("arff").toString();
		default:
			return sb.append("unkown").toString();
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
