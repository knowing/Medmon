package de.lmu.ifi.dbs.medmon.medic.ui.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Therapy;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.ImportDataSensorAndDirectoryPage;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.TherapyResultDataPage;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.TherapyResultPatientPage;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.TherapyResultTherapyPage;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

public class TherapyResultWizard extends Wizard {

	private TherapyResultPatientPage	selectPatientPage	= new TherapyResultPatientPage();
	private TherapyResultTherapyPage	selectTherapyPage	= new TherapyResultTherapyPage();
	private ImportDataSensorAndDirectoryPage	selectSensorPage	= new ImportDataSensorAndDirectoryPage();
	private TherapyResultDataPage		selectDataPage		= new TherapyResultDataPage();

	private static final Logger			log					= LoggerFactory.getLogger(Activator.PLUGIN_ID);

	private Patient						selectedPatient;
	private Therapy						selectedTherapy;
	private ISensor						selectedSensor;
	private Data						selectedData;

	public void setSelectedPatient(Patient selectedPatient) {
		this.selectedPatient = selectedPatient;
	}

	public void setSelectedTherapy(Therapy selectedTherapy) {
		this.selectedTherapy = selectedTherapy;
	}

	public void setSelectedSensor(ISensor selectedSensor) {
		this.selectedSensor = selectedSensor;
	}

	public void setSelectedData(Data selectedData) {
		this.selectedData = selectedData;
	}

	@Override
	public void addPages() {
		addPage(selectPatientPage);
		addPage(selectTherapyPage);
		addPage(selectSensorPage);
		addPage(selectDataPage);
	}

	@Override
	public boolean performFinish() {
		return true;
	}
}
