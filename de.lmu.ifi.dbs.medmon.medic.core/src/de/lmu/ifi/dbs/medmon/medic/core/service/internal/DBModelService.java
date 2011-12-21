package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createDirectory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.activation.ActivateFailedException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;

import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.database.model.Therapy;
import de.lmu.ifi.dbs.medmon.database.model.TherapyResult;
import de.lmu.ifi.dbs.medmon.medic.core.Activator;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IDBModelService;
import de.lmu.ifi.dbs.medmon.medic.core.service.IEntityManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;

public class DBModelService implements IDBModelService {

	private IGlobalSelectionProvider	selectionProvider	= null;
	private EntityManager				workerEM			= null;

	public void deactivate() {
		selectionProvider.unregister();
		workerEM.close();
	}

	public void bindEntityManagerService(IEntityManagerService service) {
		if (workerEM == null)
			workerEM = service.createEntityManager();
	}

	public void bindGlobalSelectionProvider(IGlobalSelectionProvider service) {
		if (selectionProvider == null)
			selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
	}

	/**
	 * unhooks the data completely from the db and removes it
	 * 
	 * @param d
	 * @throws IOException
	 */
	private void deleteDataTask(Data d) throws IOException {

		workerEM.getTransaction().begin();
		Data mData = workerEM.find(Data.class, d.getId());

		if (mData.getTherapyResult() != null)
			mData.getTherapyResult().setData(null);
		mData.setTherapyResult(null);

		if (mData.getPatient() != null)
			mData.getPatient().getData().remove(mData);
		mData.setPatient(null);

		if (mData.getSensor() != null)
			mData.getSensor().getData().remove(mData);
		mData.setSensor(null);

		Files.deleteIfExists(Paths.get(mData.getFile()));
		workerEM.remove(mData);

		workerEM.getTransaction().commit();
		workerEM.clear();
	}

	/**
	 * unhooks the TherapyResults completely from the db and deletes it
	 * 
	 * @param t
	 * @throws IOException
	 */
	private void deleteTherapyResultTask(TherapyResult t) throws IOException {

		workerEM.getTransaction().begin();
		TherapyResult mTherapyResult = workerEM.find(TherapyResult.class, t.getId());

		if (mTherapyResult.getData() != null)
			mTherapyResult.getData().setTherapyResult(null);
		mTherapyResult.setData(null);

		if (mTherapyResult.getTherapy() != null)
			mTherapyResult.getTherapy().getTherapyResults().remove(mTherapyResult);
		mTherapyResult.setTherapy(null);

		workerEM.remove(mTherapyResult);

		workerEM.getTransaction().commit();
		workerEM.clear();
	}

	/**
	 * 
	 * @param p
	 * @throws IOException
	 */
	private void deleteTherapyTask(Therapy t) throws IOException {

		workerEM.getTransaction().begin();
		Therapy mTherapy = workerEM.find(Therapy.class, t.getId());

		if (mTherapy.getPatient() != null)
			mTherapy.getPatient().getTherapies().remove(mTherapy);
		mTherapy.setPatient(null);

		for (TherapyResult r : mTherapy.getTherapyResults())
			r.setTherapy(null);
		mTherapy.getTherapyResults().clear();

		workerEM.remove(mTherapy);

		workerEM.getTransaction().commit();
		workerEM.clear();
	}

	/**
	 * 
	 * @param p
	 * @throws IOException
	 */
	private void deletePatientTask(Patient p) throws IOException {

		workerEM.getTransaction().begin();
		Patient mPatient = workerEM.find(Patient.class, p.getId());

		for (Data d : mPatient.getData())
			d.setPatient(null);
		mPatient.getData().clear();

		for (Therapy t : mPatient.getTherapies())
			t.setPatient(null);
		mPatient.getData().clear();

		Activator.getPatientService().releasePatient(mPatient);

		workerEM.remove(mPatient);

		workerEM.getTransaction().commit();
		workerEM.clear();
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
	private void performDeletion(final Collection entities) {

		ProgressMonitorDialog dialog = new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());

		try {
			dialog.run(false, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.beginTask("l�sche Eintr�ge", entities.size());
						for (Object e : entities) {
							if (e instanceof Data)
								deleteDataTask((Data) e);
							else if (e instanceof TherapyResult)
								deleteTherapyResultTask((TherapyResult) e);
							else if (e instanceof Therapy)
								deleteTherapyTask((Therapy) e);
							else if (e instanceof Patient)
								deletePatientTask((Patient) e);

							monitor.worked(1);
							Thread.sleep(250); // only for testing
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}

		Class<?>[] clazzes = new Class<?>[] { Patient.class, Therapy.class, TherapyResult.class, Data.class, Sensor.class };
		IGlobalSelectionProvider selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
		for (Class<?> clazz : clazzes) {
			Object currentSelection = selectionProvider.getSelection(clazz);
			if (entities.contains(currentSelection))
				selectionProvider.setSelection(clazz, null);
			else
				selectionProvider.updateSelection(clazz);
		}
		selectionProvider.unregister();
	}

	/**
	 * Deletes the patient and all connected entities also shows a
	 * ProgressDialog
	 * 
	 * @throws IOException
	 */
	@Override
	public void deletePatient(Patient p) throws IOException {

		Set<Object> entitiesToDelete = new HashSet<Object>();

		Patient mPatient = workerEM.find(Patient.class, p.getId());
		entitiesToDelete.add(mPatient);

		for (Therapy t : mPatient.getTherapies()) {
			entitiesToDelete.add(t);
			for (TherapyResult r : t.getTherapyResults()) {
				entitiesToDelete.add(r);
				if (r.getData() != null)
					entitiesToDelete.add(r.getData());
			}
		}

		workerEM.clear();

		performDeletion(entitiesToDelete);
	}

	/**
	 * Deletes the Data and all connected entities also shows a ProgressDialog
	 * 
	 * @throws IOException
	 */
	@Override
	public void deleteData(Data d) throws IOException {

		Set<Object> entitiesToDelete = new HashSet<Object>();

		Data mData = workerEM.find(Data.class, d.getId());
		entitiesToDelete.add(mData);
		if (mData.getTherapyResult() != null)
			entitiesToDelete.add(mData.getTherapyResult());
		workerEM.clear();

		performDeletion(entitiesToDelete);

	}

	/**
	 * Deletes the TherapyResult and all connected entities also shows a
	 * ProgressDialog
	 * 
	 * @throws IOException
	 */
	@Override
	public void deleteTherapyResult(TherapyResult r) throws IOException {

		Set<Object> entitiesToDelete = new HashSet<Object>();

		TherapyResult mTherapyResult = workerEM.find(TherapyResult.class, r.getId());
		entitiesToDelete.add(mTherapyResult);
		if (mTherapyResult.getData() != null)
			entitiesToDelete.add(mTherapyResult.getData());
		workerEM.clear();

		performDeletion(entitiesToDelete);

	}

	/**
	 * Deletes the Therapy and all connected entities also shows a
	 * ProgressDialog
	 * 
	 * @throws IOException
	 */
	@Override
	public void deleteTherapy(Therapy t) throws IOException {

		Set<Object> entitiesToDelete = new HashSet<Object>();

		Therapy mTherapy = workerEM.find(Therapy.class, t.getId());
		entitiesToDelete.add(mTherapy);

		for (TherapyResult r : mTherapy.getTherapyResults()) {
			entitiesToDelete.add(r);
			if (r.getData() != null)
				entitiesToDelete.add(r.getData());
		}

		workerEM.clear();

		performDeletion(entitiesToDelete);
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

		Patient mPatient = new Patient();

		workerEM.getTransaction().begin();
		workerEM.persist(mPatient);
		workerEM.getTransaction().commit();
		workerEM.clear();

		Activator.getPatientService().initializePatient(mPatient);

		return mPatient;
	}

	/**
	 * Creates a new Therapy for a Patient
	 * 
	 * @param p
	 *            - a Patient
	 * @return a new Therapy
	 */
	@Override
	public Therapy createTherapy(Patient p) {

		Therapy mTherapy = new Therapy();

		workerEM.getTransaction().begin();

		Patient mPatient = workerEM.find(Patient.class, p.getId());

		mTherapy.setCaption("neue Therapie");
		mTherapy.setComment("kein Kommentar.");
		mTherapy.setTherapyStart(new Date());
		mTherapy.setTherapyEnd(new Date());

		mTherapy.setPatient(mPatient);
		mPatient.getTherapies().add(mTherapy);

		workerEM.persist(mTherapy);
		workerEM.getTransaction().commit();
		workerEM.clear();

		selectionProvider.updateSelection(Patient.class);

		return mTherapy;
	}

	/**
	 * Creates a new TherapyResult and connects it to a Data entity and a
	 * Therapy entity
	 * 
	 * @param d
	 *            - a Data entity
	 * @param t
	 *            - a Therapy entity
	 * @return - a new TherapyResult
	 */
	@Override
	public TherapyResult createTherapyResult(Data d, Therapy t) {

		workerEM.getTransaction().begin();
		Data mData = workerEM.find(Data.class, d.getId());
		Therapy mTherapy = workerEM.find(Therapy.class, t.getId());

		TherapyResult mTherapyResult = new TherapyResult(mData, mTherapy);
		mTherapyResult.setTherapy(mTherapy);
		mTherapy.getTherapyResults().add(mTherapyResult);

		mTherapyResult.setData(mData);
		mData.setTherapyResult(mTherapyResult);

		mTherapy.getPatient().getData().add(mData);
		mData.setPatient(mTherapy.getPatient());

		mTherapyResult.setCaption("neues Ergebnis");
		mTherapyResult.setComment("kein Kommentar.");
		mTherapyResult.setSuccess(50);
		mTherapyResult.setTimestamp(null);

		workerEM.persist(mTherapyResult);

		workerEM.getTransaction().commit();
		workerEM.clear();

		selectionProvider.updateSelection(Patient.class);

		return mTherapyResult;
	}

	/**
	 * Creates a mew Data entity
	 */
	@Override
	public Data createData(Patient p, Sensor s, String type, Date from, Date to, String file) {

		Data mData = new Data();

		workerEM.getTransaction().begin();

		Patient mPatient = workerEM.find(Patient.class, p.getId());
		Sensor mSensor = workerEM.find(Sensor.class, s.getId());

		mData.setPatient(mPatient);
		mData.setSensor(mSensor);
		mData.setType(type);
		mData.setFrom(from);
		mData.setTo(to);
		mData.setFile(file);

		mPatient.getData().add(mData);
		mSensor.getData().add(mData);

		workerEM.persist(mData);

		workerEM.getTransaction().commit();
		workerEM.clear();

		return mData;
	}
}
