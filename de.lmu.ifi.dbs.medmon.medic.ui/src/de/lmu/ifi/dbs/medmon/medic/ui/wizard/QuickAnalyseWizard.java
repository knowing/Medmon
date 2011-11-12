package de.lmu.ifi.dbs.medmon.medic.ui.wizard;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.knowing.core.model.IDataProcessingUnit;
import de.lmu.ifi.dbs.knowing.core.swt.handler.EvaluateHandler;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.pages.SelectDPUPage;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.pages.SelectDataPage;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.preferences.IMedicPreferences;
import de.lmu.ifi.dbs.medmon.medic.core.sensor.SensorAdapter;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.SensorPage;
import de.lmu.ifi.dbs.medmon.sensor.core.converter.IConverter;

public class QuickAnalyseWizard extends Wizard implements INewWizard, IExecutableExtension {

	private SensorPage sourcePage = new SensorPage();
	private SelectDataPage dataPage = new SelectDataPage();
	private SelectDPUPage dpuPage;
	
	private static final Logger	log	= LoggerFactory.getLogger(Activator.PLUGIN_ID);
	
	/* to prevent getNextPage setting input twice */
	private boolean firstcall = true;

	private void initSelections(IPatientService service) {
		/* Can skip sensorPage */
		log.debug("QuickAnalyseWizard::initSelections() => Patient patient = (Patient) service.getSelection(IPatientService.PATIENT);");
		log.debug("QuickAnalyseWizard::initSelections() => SensorAdapter sensor = (SensorAdapter) service.getSelection(IPatientService.SENSOR);");
		log.debug("QuickAnalyseWizard::initSelections() => if (patient != null && sensor != null)");
		log.debug("QuickAnalyseWizard::initSelections() =>     sourcePage = new SensorPage(patient, sensor);");
		//Patient patient = (Patient) service.getSelection(IPatientService.PATIENT);
		//SensorAdapter sensor = (SensorAdapter) service.getSelection(IPatientService.SENSOR);
		//if (patient != null && sensor != null)
		//	sourcePage = new SensorPage(patient, sensor);

	}

	@Override
	public void addPages() {
		log.debug("QuickAnalyseWizard::addPages() => initSelections(service);");
		log.debug("QuickAnalyseWizard::addPages() => DataProcessingUnit dpu = (DataProcessingUnit) Activator.getPatientService().getSelection(IPatientService.DPU);");
		//initSelections(service);
		//DataProcessingUnit dpu = (DataProcessingUnit) Activator.getPatientService().getSelection(IPatientService.DPU);
		addPage(sourcePage);
		addPage(dataPage);
		addPage(dpuPage = new SelectDPUPage());
	}
	
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if(page == sourcePage) {
			dataPage.setSensor(sourcePage.getSensor());
			dataPage.setPatient(sourcePage.getPatient());
		} else if(page == dataPage) {
			dpuPage.setSensor(sourcePage.getSensor());
		}
		return super.getNextPage(page);
	}

	@Override
	public boolean performFinish() {
		log.debug("QuickAnalyseWizard::performFinish() => DataProcessingUnit dpu = (DataProcessingUnit) Activator.getPatientService().getSelection(IPatientService.DPU);");
//		DataProcessingUnit dpu = (DataProcessingUnit) Activator.getPatientService().getSelection(IPatientService.DPU);
		/*
		IDataProcessingUnit dpu = dpuPage.getSelection();
		if (dpu == null)
			return false;
		
		Patient patient = sourcePage.getPatient();
		SensorAdapter sensor = sourcePage.getSensor();
		IConverter converter = sensor.getSensorExtension().getConverter();
		Date from = dataPage.getFrom();
		Date to = dataPage.getTo();
		
		try {
			FileOutputStream out = new FileOutputStream(path);
			converter.copy(out, from, to);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
//		Node[] nodes = dpu.node("loader", converter.getId());
//		for (Node node : nodes) {
//			Properties properties = node.properties();
//			//TODO QuickAnalyseWizard -> node property TLoader.FILE 
//			properties.setProperty("file", patient.getLastname());
//		}
		//TODO QuickAnalyse Wizard -> Evaluate: THIS IS NOT TESTED YET!
		try {
			IDataProcessingUnit dummy = IDataProcessingUnit.TYPE.instantiate();
			EvaluateHandler.evaluate(dummy, new URI("file", "./" + dir, null));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		*/
		return true;
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {

	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {

	}

}
