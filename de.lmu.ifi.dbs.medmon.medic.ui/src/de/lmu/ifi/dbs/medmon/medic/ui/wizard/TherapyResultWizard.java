package de.lmu.ifi.dbs.medmon.medic.ui.wizard;

import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizardOptions.SOURCE_FILE;
import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizardOptions.SOURCE_SENSOR;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.part.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.base.ui.wizard.pages.SelectAndConfigureDPUPage;
import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Therapy;
import de.lmu.ifi.dbs.medmon.database.model.TherapyResult;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.ImportDataDataPage;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.ImportDataPatientAndTypePage;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.ImportDataSensorAndDirectoryPage;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.TherapyResultPatientAndTypePage;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.TherapyResultTherapyPage;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

/**
 * <p>
 * This class is almost identically with {@link ImportDataWizard} and uses 80%
 * of its code. In the next version both Wizard classes should inherited from an
 * abstract class.
 * </p>
 * 
 * <p>
 * The same for {@link ImportDataPatientAndTypePage}. There will be a special
 * class for this wizard as you can choose data from your db, too.
 * </p>
 * 
 * 
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 01.12.2011
 * 
 */
public class TherapyResultWizard extends Wizard {

	private TherapyResultTherapyPage			therapyPage				= new TherapyResultTherapyPage();
	private TherapyResultPatientAndTypePage		patientAndTypePage		= new TherapyResultPatientAndTypePage();
	private ImportDataSensorAndDirectoryPage	sensorAndDirectoryPage	= new ImportDataSensorAndDirectoryPage();
	private ImportDataDataPage					dataPage				= new ImportDataDataPage();
	private SelectAndConfigureDPUPage			selectDPUPage			= new SelectAndConfigureDPUPage();
	private IWizardPage							currentPage				= null;

	private static final Logger					log						= LoggerFactory.getLogger(Activator.PLUGIN_ID);
	private Therapy								preselectedTherapy;

	/**
	 * Only creates a TherapyResult for given Therapy
	 * 
	 * @param therapy
	 */
	public TherapyResultWizard(Therapy therapy) {
		this.preselectedTherapy = therapy;
	}

	@Override
	public void addPages() {
		addPage(patientAndTypePage);
		if (preselectedTherapy == null)
			addPage(therapyPage);
		addPage(sensorAndDirectoryPage);
		addPage(dataPage);
		addPage(selectDPUPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		currentPage = page;
		int options = patientAndTypePage.getOption();
		if (page == patientAndTypePage && preselectedTherapy == null) {
			therapyPage.setPatient(patientAndTypePage.getSelectedPatient());
			return therapyPage;
		} else if (page == therapyPage || (page == patientAndTypePage && preselectedTherapy != null)) {
			sensorAndDirectoryPage.setDirectorySectionEnabled(((options & SOURCE_FILE) != 0));
			sensorAndDirectoryPage.checkContents();
			return sensorAndDirectoryPage;
		} else if (page == sensorAndDirectoryPage) {
			ISensor selectedSensor = sensorAndDirectoryPage.getSelectedSensor();
			String selectedDirectory = sensorAndDirectoryPage.getSelectedDirectory();
			if ((options & SOURCE_SENSOR) != 0)
				dataPage.setInput(selectedSensor, Activator.getSensorManagerService().availableInputs(selectedSensor));
			if ((options & SOURCE_FILE) != 0) {
				List<URI> uriList = new ArrayList<URI>();
				try {
					DirectoryStream<Path> directoyStream = Files.newDirectoryStream(Paths.get(selectedDirectory));
					String filePrefix = selectedSensor.getFilePrefix();
					for (Path file : directoyStream)
						if (file.toString().endsWith(filePrefix))
							uriList.add(file.toUri());

				} catch (IOException e) {
					e.printStackTrace();
				}
				dataPage.setInput(null, uriList);
			}
			return dataPage;
		}
		return null;
	}

	@Override
	public boolean performFinish() {
		Patient selectedPatient = patientAndTypePage.getSelectedPatient();
		ISensor selectedSensor = sensorAndDirectoryPage.getSelectedSensor();
		URI selectedUri = dataPage.getSelectedURI();

		int options = patientAndTypePage.getOption();
		Data data = null;
		Data taggedData = null;

		if (((options & SOURCE_SENSOR) | (options & SOURCE_FILE)) != 0) {

			try {
				data = Activator.getPatientService().store(selectedPatient, selectedSensor, IPatientService.RAW, selectedUri).dataEntity;
			} catch (IOException e) {
				MessageDialog.openError(getShell(), "Daten konnten nicht importiert werden", e.getMessage());
				e.printStackTrace();
				return false;
			}

			if (currentPage == selectDPUPage) {
				try {
					taggedData = selectDPUPage.configureAndExecuteDPU(selectedPatient, data);
				} catch (IOException e) {
					e.printStackTrace();
					try {
						Activator.getPatientService().remove(data);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					MessageDialog.openError(getShell(), "Fehler beim Ausfuehren des Klassifikationsprozesses", e.getMessage());
					return false;
				}
			}

			try {
				PlatformUI.getWorkbench().showPerspective("de.lmu.ifi.dbs.medmon.medic.ui.default",
						PlatformUI.getWorkbench().getActiveWorkbenchWindow());
			} catch (WorkbenchException e) {
				e.printStackTrace();
			}
		}

		try {
			TherapyResult mTherapyResult = new TherapyResult();
			Therapy therapy = Activator.getGlobalSelectionService().getSelection(Therapy.class);

			// create EM
			EntityManager entityManager = JPAUtil.createEntityManager();
			
			// begin and find
			entityManager.getTransaction().begin();
			Data mData = entityManager.find(Data.class, data.getId());
			Therapy mTherapy = entityManager.find(Therapy.class, therapy.getId());
			
			// therapy <-> therapyResult
			mTherapyResult.setTherapy(mTherapy);
			mTherapy.getTherapyResults().add(mTherapyResult);

			// therapyResult <-> data
			mTherapyResult.setData(mData);
			mData.setTherapyResult(mTherapyResult);

			// therapyResult
			mTherapyResult.setCaption("neues Ergebnis");
			mTherapyResult.setComment("kein Kommentar.");
			mTherapyResult.setSuccess(50);
			mTherapyResult.setTimestamp(null);

			//persist
			entityManager.persist(mTherapyResult);
			
			// commit and close
			entityManager.getTransaction().commit();
			entityManager.close();

			IGlobalSelectionProvider selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
			selectionProvider.updateSelection(Patient.class);
			selectionProvider.unregister();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				Activator.getPatientService().remove(data);
				Activator.getPatientService().remove(taggedData);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		return true;
	}
}
