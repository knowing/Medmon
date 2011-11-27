package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.walkFileTree;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.READ;

import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

import javax.persistence.EntityManager;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.medic.core.service.IEntityManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionService;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;
import de.lmu.ifi.dbs.medmon.medic.core.service.ISensorManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.util.DeleteDirectoryVisitor;
import de.lmu.ifi.dbs.medmon.sensor.core.IConverter;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

public class PatientService implements IPatientService {

	private final Logger			log						= LoggerFactory.getLogger(IGlobalSelectionService.class);

	/** Static application directory in {user.home}/.medmon */
	private final Path				medmon					= Paths.get(System.getProperty("user.home"), ".medmon", "patients");

	/** Format patient-id to 12 digits */
	private final DecimalFormat		decimalF				= new DecimalFormat("000000000000");

	/** Format dates with DateFormat.SHORT */
	private final DateFormat		dateF					= DateFormat.getDateInstance(DateFormat.SHORT);

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

	/**
	 * <p>
	 * Create root directory with three subdirectories TRAIN/RESULT/RAW
	 * </p>
	 * 
	 * @return Patient
	 * @throws IOException
	 */
	@Override
	public Patient createPatient() throws IOException {
		EntityManager em = entityManagerService.createEntityManager();
		em.getTransaction().begin();
		Patient patient = new Patient();
		em.persist(patient);
		em.getTransaction().commit();
		em.close();
		Path root = createDirectories(locateDirectory(patient, ROOT));
		createDirectory(root.resolve(TRAIN));
		createDirectory(root.resolve(RESULT));
		createDirectory(root.resolve(RAW));
		return patient;
	}

	/**
	 * <p>
	 * Deletes first the source and then the db entities
	 * </p>
	 * 
	 * @param - Patient to delete
	 * @throws IOException
	 */
	@Override
	public void deletePatient(Patient p) throws IOException {
		walkFileTree(locateDirectory(p, ROOT), new DeleteDirectoryVisitor());
		EntityManager em = entityManagerService.createEntityManager();
		em.getTransaction().begin();
		Patient patient = em.merge(p);
		em.remove(patient);
		em.getTransaction().commit();
	}

	/**
	 * <p>
	 * Creates a new file and {@link Data} instance and returns the
	 * corresponding OutputStream.
	 * </p>
	 * 
	 * @param p
	 *            -
	 * @param s
	 *            -
	 * @param type
	 *            -
	 * @param from
	 *            -
	 * @param to
	 *            -
	 * @return Ready to write OutputStream
	 * @throws IOException
	 */
	@Override
	public OutputStream store(Patient p, Sensor s, String type, Date from, Date to) throws IOException {
		EntityManager em = entityManagerService.createEntityManager();
		em.getTransaction().begin();
		Patient patient = em.merge(p);
		Sensor sensor = em.merge(s);
		Data data = new Data(patient, sensor, type, from, to);
		Path file = locateDirectory(patient, type).resolve(generateFilename(sensor, type, from, to));
		data.setFile(file.toString());
		em.persist(data);
		em.getTransaction().commit();
		em.close();
		return newOutputStream(file, CREATE_NEW);
	}

	/**
	 * 
	 */
	@Override
	public void store(Patient p, ISensor s, String type) {

		DirectoryStream<Path> directoyStream = null;
		try (DirectoryStream<Path> newDirectoyStream = Files.newDirectoryStream(locateDirectory(p, type))) {
			directoyStream = newDirectoyStream;
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (directoyStream == null || !directoyStream.iterator().hasNext())
			return;

		IConverter converter = null;
		try {
			converter = s.newConverter(newInputStream(directoyStream.iterator().next()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (converter == null)
			return;

		Sensor entity = sensorManagerService.loadSensorEntity(s);
		OutputStream os = null;
		try {
			os = store(p, entity, type, converter.getInterval().getStart().toDate(), converter.getInterval().getEnd().toDate());
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (os == null)
			return;

		// TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		// InputStream is = converter.getInputStream();
		InputStream is = null;

		byte[] buffer = new byte[4096];
		int bytesRead = 0;
		try {
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
		} catch (Exception e) {
			e.printStackTrace();
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

		return store(d1.getPatient(), d1.getSensor(), d1.getType(), from, to);
	}

	/**
	 * <p>
	 * Deletes the source and then the db entity
	 * </p>
	 * 
	 * @param d
	 *            - detached {@link Data} object
	 * @throws IOException
	 */
	@Override
	public void remove(Data d) throws IOException {
		Files.delete(locateFile(d));
		EntityManager em = entityManagerService.createEntityManager();
		em.getTransaction().begin();
		Data data = em.merge(d);
		em.remove(data);
		em.getTransaction().commit();
		em.close();
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
	 * For date formatting {@link DateFormat.SHORT} is used.
	 * </p>
	 * 
	 * @param s
	 *            - Sensor
	 * @param type
	 *            - RAW, TRAIN or RESULT
	 * @param from
	 *            - Data recording start
	 * @param to
	 *            - Data recording end
	 * @return
	 */
	private String generateFilename(Sensor s, String type, Date from, Date to) {
		StringBuilder sb = new StringBuilder(32);
		sb.append(dateF.format(from)).append("_to_").append(dateF.format(to)).append(".");
		switch (type) {
		case RAW:
			return sb.append(s.getId()).append(".").append("filePrefix").toString();
		case TRAIN:
			return sb.append("arff").toString();
		case RESULT:
			return sb.append("arff").toString();
		default:
			return sb.append("unkown").toString();
		}
	}

	protected void activate(ComponentContext context) {
		System.out.println("PatientService started successfully");
	}

	protected void bindEntityManager(IEntityManagerService service) {
		entityManagerService = service;
	}

	protected void unbindEntityManager(IEntityManagerService service) {
		entityManagerService = null;
	}

	protected void bindSensorService(ISensorManagerService service) {
		sensorManagerService = service;
	}

	protected void unbindSensorService(ISensorManagerService service) {
		sensorManagerService = null;
	}

}
