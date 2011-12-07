package de.lmu.ifi.dbs.medmon.medic.ui.wizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.base.ui.wizard.pages.SelectAndConfigureDPUPage;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
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
	private SelectAndConfigureDPUPage	selectDPUPage		= new SelectAndConfigureDPUPage();

	private static final Logger			log					= LoggerFactory.getLogger(Activator.PLUGIN_ID);
	
	@Override
	public void addPages() {
		addPage(selectPatientPage);
		addPage(selectSensorPage);
		addPage(selectDPUPage);
//		addPage(selectTherapyPage);
//		addPage(selectDataPage);
	}
	
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage nextPage = super.getNextPage(page);
		
		
		return nextPage;
	}

	@Override
	public boolean performFinish() {
		Patient patient = selectPatientPage.getPatient();
		//set error msg here
		if(patient == null)
			return false;
		
		ISensor sensor = selectSensorPage.getSelectedSensor();
		if(sensor == null)
			return false;
		
		//selectDPUPage.configureAndExecuteDPU(patient, data);
		
		return true;
	}
}
