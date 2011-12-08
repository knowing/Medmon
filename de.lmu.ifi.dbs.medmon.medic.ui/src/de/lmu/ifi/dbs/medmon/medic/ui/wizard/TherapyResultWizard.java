package de.lmu.ifi.dbs.medmon.medic.ui.wizard;

import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizardOptions.IMPORT_RAW;
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
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.base.ui.wizard.pages.SelectAndConfigureDPUPage;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Therapy;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.ImportDataDataPage;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.ImportDataPatientAndTypePage;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.ImportDataSensorAndDirectoryPage;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

/**
 * <p>This class is almost identically with {@link ImportDataWizard} and uses 80% of
 * its code. In the next version both Wizard classes should inherited from an abstract
 * class.</p> 
 * 
 * <p>The same for {@link ImportDataPatientAndTypePage}. There will be a special class
 * for this wizard as you can choose data from your db, too.</p>
 * 
 * 
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 01.12.2011
 *
 */
public class TherapyResultWizard extends Wizard {

	private ImportDataPatientAndTypePage		patientAndTypePage		= new ImportDataPatientAndTypePage();
	private ImportDataSensorAndDirectoryPage	sensorAndDirectoryPage	= new ImportDataSensorAndDirectoryPage();
	private ImportDataDataPage					dataPage				= new ImportDataDataPage();
	private SelectAndConfigureDPUPage			selectDPUPage			= new SelectAndConfigureDPUPage();

	private static final Logger					log						= LoggerFactory.getLogger(Activator.PLUGIN_ID);
	private Therapy								therapy;

	/**
	 * Only creates a TherapyResult for given Therapy
	 * @param therapy
	 */
	public TherapyResultWizard(Therapy therapy) {
		this.therapy = therapy;
	}

	@Override
	public void createPageControls(Composite pageContainer) {
		if (therapy == null) {
			MessageDialog
					.openWarning(getShell(), "Keine Therapie ausgewaehl", "Bitte waehlen Sie zuerst eine Therapie aus um fortzufahren");
		}
		super.createPageControls(pageContainer);
	}

	@Override
	public void addPages() {
		addPage(patientAndTypePage);
		addPage(sensorAndDirectoryPage);
		addPage(dataPage);
		addPage(selectDPUPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		int options = patientAndTypePage.getOption();
		if (page == patientAndTypePage) {
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
					for (Path file : directoyStream)
						uriList.add(file.toUri());

				} catch (IOException e) {
					e.printStackTrace();
				}
				dataPage.setInput(null, uriList);
			}
			return dataPage;
		}
		return super.getNextPage(page);
	}

	@Override
	public boolean performFinish() {
		Patient patient = patientAndTypePage.getSelectedPatient();
		// set error msg here
		if (patient == null)
			return false;

		ISensor sensor = sensorAndDirectoryPage.getSelectedSensor();
		if (sensor == null)
			return false;
		
		URI uri = dataPage.getSelectedURI();
		if(uri == null)
			return false;

		//This block should only be triggered if user selects import before analyze data
/*		IPatientService patientService = Activator.getPatientService();
		int options = patientAndTypePage.getOption();
		
		if ((options & (SOURCE_SENSOR | IMPORT_RAW)) != 0) {
			try {
				//We need the created Data entity here!
				patientService.store(patient, sensor, IPatientService.RAW, uri);
				IGlobalSelectionProvider selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
				selectionProvider.updateSelection(Patient.class);
				selectionProvider.unregister();
			} catch (IOException e) {
				MessageDialog.openError(getShell(), "Daten konnten nicht importiert werden", e.getMessage());
				e.printStackTrace();
			}
		} else if ((options & (SOURCE_FILE | IMPORT_RAW)) != 0) {
			try {
				//We need the created Data entity here!
				patientService.store(patient, sensor, IPatientService.RAW, uri);
				IGlobalSelectionProvider selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
				selectionProvider.updateSelection(Patient.class);
				selectionProvider.unregister();
			} catch (IOException e) {
				MessageDialog.openError(getShell(), "Daten konnten nicht importiert werden", e.getMessage());
				e.printStackTrace();
			}
		}*/
		
		try {
			selectDPUPage.configureAndExecuteDPU(patient, sensor, uri);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		// selectDPUPage.configureAndExecuteDPU(patient, data);

		return true;
	}
}
