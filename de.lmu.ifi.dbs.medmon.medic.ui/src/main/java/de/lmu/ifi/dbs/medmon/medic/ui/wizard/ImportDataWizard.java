package de.lmu.ifi.dbs.medmon.medic.ui.wizard;

import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizardOptions.IMPORT_RAW;
import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizardOptions.SOURCE_FILE;

import java.io.IOException;
import java.net.URI;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import de.lmu.ifi.dbs.medmon.base.ui.wizard.pages.ImportDataDataPage;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.pages.ImportDataSensorAndDirectoryPage;
import de.lmu.ifi.dbs.medmon.database.entity.Data;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.ImportDataPatientAndTypePage;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;
import de.lmu.ifi.dbs.medmon.services.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.services.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.services.IPatientService;

public class ImportDataWizard extends Wizard {

	private ImportDataPatientAndTypePage		patientAndTypePage;
	private ImportDataSensorAndDirectoryPage	sensorAndDirectoryPage;
	private ImportDataDataPage					dataPage;

	private Patient								selectedPatient;
	private ISensor								selectedSensor;
	private String								selectedDirectory;
	private URI									selectedURI;

	public ImportDataWizard() {
		setWindowTitle("New Wizard");
	}

	@Override
	public void addPages() {
		addPage(patientAndTypePage = new ImportDataPatientAndTypePage());
		addPage(sensorAndDirectoryPage = new ImportDataSensorAndDirectoryPage());
		addPage(dataPage = new ImportDataDataPage());
	}

	@Override
	public boolean performFinish() {
		selectedPatient = patientAndTypePage.getSelectedPatient();
		selectedDirectory = sensorAndDirectoryPage.getSelectedDirectory();
		selectedSensor = sensorAndDirectoryPage.getSelectedSensor();

		IPatientService patientService = Activator.getPatientService();

		int options = patientAndTypePage.getOption();
		if ((options & IMPORT_RAW) != 0) {
			try {
				selectedURI = dataPage.getSelectedURI();
				Data data = patientService.store(selectedPatient, selectedSensor, Data.RAW);
				IGlobalSelectionProvider selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
				selectionProvider.setSelection(Patient.class, selectedPatient);
				selectionProvider.setSelection(Data.class, data);
				selectionProvider.unregister();
				PlatformUI.getWorkbench().showPerspective("de.lmu.ifi.dbs.medmon.medic.ui.default",
						PlatformUI.getWorkbench().getActiveWorkbenchWindow());
			} catch (IOException | WorkbenchException e) {
				MessageDialog.openError(getShell(), "Daten konnten nicht importiert werden", e.getMessage());
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		int options = patientAndTypePage.getOption();
		if (page == patientAndTypePage) {
			sensorAndDirectoryPage.setDirectorySectionEnabled(((options & SOURCE_FILE) != 0));
			sensorAndDirectoryPage.checkContents();
			prepareDataPage(options);
			return sensorAndDirectoryPage;
		} else if (page == sensorAndDirectoryPage) {
			prepareDataPage(options);
			return dataPage;
		}
		return null;
	}

	private void prepareDataPage(int options) {
		selectedSensor = sensorAndDirectoryPage.getSelectedSensor();
		selectedDirectory = sensorAndDirectoryPage.getSelectedDirectory();
		//if ((options & SOURCE_SENSOR) != 0)
			dataPage.setInput(selectedSensor, Activator.getSensorManagerService().getConnectedSensors());
		
//		if ((options & SOURCE_FILE) != 0) {
//			List<URI> uriList = new ArrayList<URI>();
//			try (DirectoryStream<Path> directoyStream = Files.newDirectoryStream(Paths.get(selectedDirectory))) {
//				String filePrefix = selectedSensor.getFilePrefix();
//				for (Path file : directoyStream) {
//					// Don't use file.endsWith() -> checks the last foldername
//					// of the path not the prefix of the file
//					if (file.toString().endsWith(filePrefix))
//						uriList.add(file.toUri());
//				}
//
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			dataPage.setInput(null, uriList);
//		}
	}
}
