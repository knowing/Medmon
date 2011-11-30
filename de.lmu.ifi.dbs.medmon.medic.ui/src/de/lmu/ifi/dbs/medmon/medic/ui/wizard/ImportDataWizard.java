package de.lmu.ifi.dbs.medmon.medic.ui.wizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.ImportDataPatientAndTypePage;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.ImportDataSensorPage;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;
import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.ImportDataPatientAndTypePage.*;
import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizardOptions.*;

public class ImportDataWizard extends Wizard {

	private ImportDataPatientAndTypePage	patientAndTypePage;
	private ImportDataSensorPage			sensorPage;
	private int								options;

	private Patient							selectedPatient;
	private ISensor							selectedSensor;
	
	public void setSelectedPatient(Patient selectedPatient) {
		this.selectedPatient = selectedPatient;
	}
	
	public void setSelectedSensor(ISensor selectedSensor) {
		this.selectedSensor = selectedSensor;
	}
	
	public ImportDataWizard() {
		setWindowTitle("New Wizard");
	}

	@Override
	public void addPages() {

		addPage(patientAndTypePage = new ImportDataPatientAndTypePage());
		addPage(sensorPage = new ImportDataSensorPage());
	}

	@Override
	public boolean performFinish() {
		IPatientService patientService = Activator.getPatientService();

		if ((options & SOURCE_SENSOR & IMPORT_RAW) != 0) {
			patientService.store(selectedPatient, selectedSensor, "RAW");
		}

		return false;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page instanceof ImportDataPatientAndTypePage) {
			options = ((ImportDataPatientAndTypePage) page).getOption();
			if ((options & SOURCE_SENSOR) != 0)
				return sensorPage;
		}

		return null;
	}

}
