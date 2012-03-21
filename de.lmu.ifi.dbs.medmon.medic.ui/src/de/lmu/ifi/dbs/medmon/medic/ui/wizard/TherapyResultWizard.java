package de.lmu.ifi.dbs.medmon.medic.ui.wizard;

import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizardOptions.SOURCE_FILE;
import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizardOptions.SOURCE_SENSOR;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.knowing.core.model.IDataProcessingUnit;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.pages.SelectAndConfigureDPUPage;
import de.lmu.ifi.dbs.medmon.database.entity.Data;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.database.entity.Therapy;
import de.lmu.ifi.dbs.medmon.database.entity.TherapyResult;
import de.lmu.ifi.dbs.medmon.medic.core.service.ITherapyResultService;
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
	public boolean canFinish() {
		for (IWizardPage page : getPages()) {
			System.out.println(page.getTitle() + "   " + page.isPageComplete());
		}

		return super.canFinish();
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		int options = patientAndTypePage.getOption();
		if (page == patientAndTypePage && preselectedTherapy == null) {
			therapyPage.setPatient(patientAndTypePage.getSelectedPatient());
			return therapyPage;
		} else if (page == therapyPage || (page == patientAndTypePage && preselectedTherapy != null)) {
			sensorAndDirectoryPage.setDirectorySectionEnabled(((options & SOURCE_FILE) != 0));
			sensorAndDirectoryPage.checkContents();
			prepateDataPage(options);
			return sensorAndDirectoryPage;
		} else if (page == sensorAndDirectoryPage) {
			prepateDataPage(options);
			return dataPage;
		}
		return super.getNextPage(page);
	}

	private void prepateDataPage(int options) {
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
	}

	@Override
	public boolean performFinish() {
		Patient selectedPatient = patientAndTypePage.getSelectedPatient();
		ISensor selectedSensor = sensorAndDirectoryPage.getSelectedSensor();
		URI selectedUri = dataPage.getSelectedURI();

		int options = patientAndTypePage.getOption();
		Data data = null;

		if (((options & SOURCE_SENSOR) | (options & SOURCE_FILE)) != 0) {

			try {
				data = Activator.getPatientService().store(selectedPatient, selectedSensor, Data.RAW, selectedUri);
			} catch (IOException e) {
				MessageDialog.openError(getShell(), "Daten konnten nicht importiert werden", e.getMessage());
				e.printStackTrace();
				return false;
			}

			try {
				IDataProcessingUnit dpu = selectDPUPage.getDataProcessingUnit();
				ITherapyResultService resultService = Activator.getTherapyResultService();
				TherapyResult results = resultService.createTherapyResult(dpu, selectedPatient, preselectedTherapy, data);
				Activator.getGlobalSelectionService().setSelection(TherapyResult.class, results);
				Activator.getGlobalSelectionService().updateSelection(Therapy.class);
				Activator.getGlobalSelectionService().updateSelection(Patient.class);
				log.debug("Therapy Results created " + results);
			} catch (Exception e) {
				e.printStackTrace();
				MessageDialog.openError(getShell(), "Fehler beim Ausfuehren des Klassifikationsprozesses", e.getMessage());
				return false;
			}

			try {
				PlatformUI.getWorkbench().showPerspective("de.lmu.ifi.dbs.medmon.medic.ui.default",
						PlatformUI.getWorkbench().getActiveWorkbenchWindow());
			} catch (WorkbenchException e) {
				e.printStackTrace();
			}
		}

		return true;
	}
}
