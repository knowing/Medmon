package de.lmu.ifi.dbs.medmon.medic.ui.wizard;

import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizardOptions.IMPORT_RAW;
import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizardOptions.SOURCE_FILE;
import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizardOptions.SOURCE_SENSOR;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;
import de.lmu.ifi.dbs.medmon.medic.core.util.DataStoreOutput;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.ImportDataDataPage;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.ImportDataPatientAndTypePage;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.ImportDataSensorAndDirectoryPage;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

public class ImportDataWizard extends Wizard {

	private ImportDataPatientAndTypePage		patientAndTypePage;
	private ImportDataSensorAndDirectoryPage	sensorAndDirectoryPage;
	private ImportDataDataPage					dataPage;
	private int									options;

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

		if ((options & (SOURCE_SENSOR | IMPORT_RAW)) != 0) {
			try {
				selectedURI = dataPage.getSelectedURI();
				DataStoreOutput output = patientService.store(selectedPatient, selectedSensor, IPatientService.RAW, selectedURI);
				IGlobalSelectionProvider selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
				selectionProvider.setSelection(Patient.class, selectedPatient);
				selectionProvider.setSelection(Data.class, output.dataEntity);
				selectionProvider.unregister();
			} catch (IOException e) {
				MessageDialog.openError(getShell(), "Daten konnten nicht importiert werden", e.getMessage());
				e.printStackTrace();
			}
			return true;
		} else if ((options & (SOURCE_FILE | IMPORT_RAW)) != 0) {
			try {
				selectedURI = dataPage.getSelectedURI();
				DataStoreOutput output = patientService.store(selectedPatient, selectedSensor, IPatientService.RAW, selectedURI);
				IGlobalSelectionProvider selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
				selectionProvider.setSelection(Patient.class, selectedPatient);
				selectionProvider.setSelection(Data.class, output.dataEntity);
				selectionProvider.unregister();
			} catch (IOException e) {
				MessageDialog.openError(getShell(), "Daten konnten nicht importiert werden", e.getMessage());
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == patientAndTypePage) {
			options = patientAndTypePage.getOption();
			sensorAndDirectoryPage.setDirectorySectionEnabled(((options & SOURCE_FILE) != 0));
			sensorAndDirectoryPage.checkContents();
			return sensorAndDirectoryPage;
		} else if (page == sensorAndDirectoryPage) {
			options = patientAndTypePage.getOption();
			selectedSensor = sensorAndDirectoryPage.getSelectedSensor();
			selectedDirectory = sensorAndDirectoryPage.getSelectedDirectory();
			if ((options & SOURCE_SENSOR) != 0)
				dataPage.setInput(selectedSensor, Activator.getSensorManagerService().availableInputs(selectedSensor));
			if ((options & SOURCE_FILE) != 0) {
				List<URI> uriList = new ArrayList<URI>();
				try {
					DirectoryStream<Path> directoyStream = Files.newDirectoryStream(Paths.get(selectedDirectory));
					for (Path file : directoyStream)
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
}
