package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createDirectory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
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
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;

public class DBModelService implements IDBModelService {

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

		if (mData.getPatient() != null)
			mData.getPatient().getData().remove(mData);
		mData.setPatient(null);

		if (mData.getSensor() != null)
			mData.getSensor().getData().remove(mData);
		mData.setSensor(null);

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
		mTherapyResult.setData(null);

		if (mTherapyResult.getTherapy() != null)
			mTherapyResult.getTherapy().getTherapyResults().remove(mTherapyResult);
		mTherapyResult.setTherapy(null);

		tempEM.remove(mTherapyResult);

		tempEM.getTransaction().commit();
		tempEM.close();
	}

	/**
	 * 
	 * @param p
	 * @throws IOException
	 */
	private void deleteTherapyTask(Therapy t) throws IOException {

		EntityManager tempEM = JPAUtil.createEntityManager();
		tempEM.getTransaction().begin();
		Therapy mTherapy = tempEM.find(Therapy.class, t.getId());

		if (mTherapy.getPatient() != null)
			mTherapy.getPatient().getTherapies().remove(mTherapy);
		mTherapy.setPatient(null);

		for (TherapyResult r : mTherapy.getTherapyResults())
			r.setTherapy(null);
		mTherapy.getTherapyResults().clear();

		tempEM.remove(mTherapy);

		tempEM.getTransaction().commit();
		tempEM.close();
	}

	/**
	 * 
	 * @param p
	 * @throws IOException
	 */
	private void deletePatientTask(Patient p) throws IOException {

		EntityManager tempEM = JPAUtil.createEntityManager();
		tempEM.getTransaction().begin();
		Patient mPatient = tempEM.find(Patient.class, p.getId());

		for (Data d : mPatient.getData())
			d.setPatient(null);
		mPatient.getData().clear();

		for (Therapy t : mPatient.getTherapies())
			t.setPatient(null);
		mPatient.getData().clear();

		tempEM.remove(mPatient);

		tempEM.getTransaction().commit();
		tempEM.close();
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
						monitor.beginTask("lösche Einträge", entities.size());
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

		EntityManager tempEM = JPAUtil.createEntityManager();
		Patient mPatient = tempEM.find(Patient.class, p.getId());
		entitiesToDelete.add(mPatient);

		for (Therapy t : mPatient.getTherapies()) {
			entitiesToDelete.add(t);
			for (TherapyResult r : t.getTherapyResults()) {
				entitiesToDelete.add(r);
				if (r.getData() != null)
					entitiesToDelete.add(r.getData());
			}
		}

		tempEM.close();

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

		EntityManager tempEM = JPAUtil.createEntityManager();
		Data mData = tempEM.find(Data.class, d.getId());
		entitiesToDelete.add(mData);
		if (mData.getTherapyResult() != null)
			entitiesToDelete.add(mData.getTherapyResult());
		tempEM.close();

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

		EntityManager tempEM = JPAUtil.createEntityManager();
		TherapyResult mTherapyResult = tempEM.find(TherapyResult.class, r.getId());
		entitiesToDelete.add(mTherapyResult);
		if (mTherapyResult.getData() != null)
			entitiesToDelete.add(mTherapyResult.getData());
		tempEM.close();

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

		EntityManager tempEM = JPAUtil.createEntityManager();
		Therapy mTherapy = tempEM.find(Therapy.class, t.getId());
		entitiesToDelete.add(mTherapy);

		for (TherapyResult r : mTherapy.getTherapyResults()) {
			entitiesToDelete.add(r);
			if (r.getData() != null)
				entitiesToDelete.add(r.getData());
		}

		tempEM.close();

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

		EntityManager tempEM = JPAUtil.createEntityManager();
		tempEM.getTransaction().begin();
		tempEM.persist(mPatient);
		tempEM.getTransaction().commit();
		tempEM.close();

		Activator.getPatientService().initializePatient(mPatient);

		return mPatient;
	}
}
