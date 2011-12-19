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
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.joda.time.Interval;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.filters.unsupervised.attribute.Remove;

import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.database.model.Therapy;
import de.lmu.ifi.dbs.medmon.database.model.TherapyResult;
import de.lmu.ifi.dbs.medmon.medic.core.Activator;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IEntityManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;
import de.lmu.ifi.dbs.medmon.medic.core.service.ISensorManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.util.DataStoreOutput;
import de.lmu.ifi.dbs.medmon.medic.core.util.DeleteDirectoryVisitor;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.sensor.core.IConverter;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

public class PatientService implements IPatientService {

	private final Logger			log						= LoggerFactory.getLogger(IPatientService.class);

	/** Static application directory in {user.home}/.medmon */
	private final Path				medmon					= Paths.get(System.getProperty("user.home"), ".medmon", "patients");

	/** Format patient-id to 12 digits */
	private final DecimalFormat		decimalF				= new DecimalFormat("000000000000");

	/** Format dates with DateFormat.SHORT */
	private final DateFormat		dateF					= DateFormat.getDateInstance(DateFormat.MEDIUM);

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
		EntityManager tempEM = entityManagerService.createEntityManager();
		tempEM.getTransaction().begin();
		Patient patient = new Patient();
		tempEM.persist(patient);
		tempEM.getTransaction().commit();
		tempEM.close();
		Path root = createDirectories(locateDirectory(patient, ROOT));
		createDirectory(root.resolve(TRAIN));
		createDirectory(root.resolve(RESULT));
		createDirectory(root.resolve(RAW));
		return patient;
	}

	/**
	 * unhooks the data completely from the db and removes it
	 * 
	 * @param d
	 * @throws IOException
	 */
	private void deleteDataTask(Data d) throws IOException {

		EntityManager tempEM = JPAUtil.createEntityManager();
		tempEM.getTransaction().begin();
		Data mData = tempEM.find(Data.class, d.getId());

		if (mData.getTherapyResult() != null)
			mData.getTherapyResult().setData(null);
		mData.setTherapyResult(null);
		mData.getPatient().getData().remove(mData);
		mData.getSensor().getData().remove(mData);

		Files.deleteIfExists(Paths.get(mData.getFile()));
		tempEM.remove(mData);

		tempEM.getTransaction().commit();
		tempEM.close();
	}

	/**
	 * unhooks the TherapyResults completely from the db and deletes it
	 * 
	 * @param t
	 * @throws IOException
	 */
	private void deleteTherapyResultTask(TherapyResult t) throws IOException {

		EntityManager tempEM = JPAUtil.createEntityManager();
		tempEM.getTransaction().begin();
		TherapyResult mTherapyResult = tempEM.find(TherapyResult.class, t.getId());

		if (mTherapyResult.getData() != null)
			mTherapyResult.getData().setTherapyResult(null);
		mTherapyResult.getTherapy().getTherapyResults().remove(mTherapyResult);

		tempEM.remove(mTherapyResult);

		tempEM.getTransaction().commit();
		tempEM.close();
	}

	/**
	 * 
	 * @param p
	 * @throws IOException
	 */
	private void deletePatientTask(Patient p) throws IOException {
		// TODO
	}
	

	/**
	 * 
	 * @param p
	 * @throws IOException
	 */
	private void deleteTherapyTask(Therapy t) throws IOException {
		// TODO
	}
	

	/**
	 * performs the removal of all entities given as paramteres this methods
	 * delegates the deletion process to the delete<Class>task() methods.
	 * internaly this method only handles the Progressdialog and the
	 * SelectionService updates.
	 * 
	 * @param data
	 * @param results
	 */
	private void executeDeletion(final Data[] data, final TherapyResult[] results /*
																				 * ...
																				 * patients
																				 * ,
																				 * sensors
																				 * ,
																				 * etc
																				 * ...
																				 */) {

		ProgressMonitorDialog dialog = new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());

		try {
			dialog.run(false, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.beginTask("l�sche Daten", data.length);
						for (Data d : data) {
							deleteDataTask(d);
							monitor.worked(1);
							Thread.sleep(1000); // only for testing
						}
						monitor.beginTask("l�sche Ergebnisse", results.length);
						for (TherapyResult r : results) {
							deleteTherapyResultTask(r);
							monitor.worked(1);
							Thread.sleep(1000); // only for testing
						}

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}

		// tell the Selection Service about possible removals
		IGlobalSelectionProvider selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());

		if (data != null)
			if (data.length != 0)
				selectionProvider.setSelection(Data.class, null);
		if (results != null)
			if (results.length != 0)
				selectionProvider.updateSelection(Patient.class);

		selectionProvider.updateSelection(Patient.class);
		selectionProvider.unregister();
	}

	/**
	 * Deletes the patient and all connected entities
	 * also shows a ProgressDialog
	 * @throws IOException
	 */
	@Override
	public void deletePatient(Patient p) throws IOException {
		try {
			throw new Exception("deletePatient() -> UNIMPLEMENTED METHOD");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deletes the Data and all connected entities
	 * also shows a ProgressDialog
	 * @throws IOException
	 */
	@Override
	public void deleteData(Data d) throws IOException {

		EntityManager tempEM = JPAUtil.createEntityManager();
		Data mData = tempEM.find(Data.class, d.getId());
		TherapyResult[] mTherapyResults = (mData.getTherapyResult() == null) ? null : new TherapyResult[] { mData.getTherapyResult() };
		tempEM.close();

		executeDeletion(new Data[] { mData }, mTherapyResults);

	}

	/**
	 * Deletes the TherapyResult and all connected entities
	 * also shows a ProgressDialog
	 * @throws IOException
	 */
	@Override
	public void deleteTherapyResult(TherapyResult r) throws IOException {

		EntityManager tempEM = JPAUtil.createEntityManager();
		TherapyResult mTherapyResult = tempEM.find(TherapyResult.class, r.getId());
		Data[] mData = (mTherapyResult.getData() == null) ? null : new Data[] { mTherapyResult.getData() };
		tempEM.close();

		executeDeletion(mData, new TherapyResult[] { mTherapyResult });

	}

	/**
	 * Deletes the Therapy and all connected entities
	 * also shows a ProgressDialog
	 * @throws IOException
	 */
	@Override
	public void deleteTherapy(Therapy t) throws IOException {
		try {
			throw new Exception("deleteTherapy() -> UNIMPLEMENTED METHOD");
		} catch (Exception e) {
			e.printStackTrace();
		}

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
		EntityManager tempEM = entityManagerService.createEntityManager();
		tempEM.getTransaction().begin();
		Patient mPatient = tempEM.find(Patient.class, p.getId());
		Sensor mSensor = tempEM.find(Sensor.class, s.getId());

		Data mData = new Data(mPatient, mSensor, type, from, to);
		Path file = locateDirectory(mPatient, type).resolve(generateFilename(mSensor, type, from, to));

		// <-> bidirectional
		mData.setFile(file.toString());
		mSensor.getData().add(mData);
		mPatient.getData().add(mData);

		OutputStream outputStream = null;
		try {
			outputStream = newOutputStream(file, CREATE_NEW);
		} catch (IOException e) {
			tempEM.close();
			throw e;
		}

		tempEM.persist(mData);
		tempEM.getTransaction().commit();
		tempEM.close();
		return new DataStoreOutput(outputStream, mData);
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
	 * @param inputURL
	 */
	@Override
	public DataStoreOutput store(Patient patient, ISensor sensor, String type, URI inputURL) throws IOException {
		IConverter converter = sensorManagerService.createConverter(sensor);

		if (converter == null)
			return null;

		Sensor entity = sensorManagerService.loadSensorEntity(sensor);
		Interval interval = converter.getInterval();

		try (DataStoreOutput output = store(patient, entity, type, interval.getStart().toDate(), interval.getEnd().toDate());
				OutputStream os = output.outputStream;
				InputStream in = new FileInputStream(new File(inputURL))) {

			byte[] buffer = new byte[4096];
			int bytesRead = 0;
			while ((bytesRead = in.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
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
	 * @param s
	 *            Sensor
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
