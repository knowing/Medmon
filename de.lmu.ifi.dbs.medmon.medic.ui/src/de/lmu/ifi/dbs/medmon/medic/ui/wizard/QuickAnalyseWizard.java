package de.lmu.ifi.dbs.medmon.medic.ui.wizard;

import static de.lmu.ifi.dbs.medmon.medic.core.util.ApplicationConfigurationUtil.getPatientFolder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import de.lmu.ifi.dbs.knowing.core.graph.Node;
import de.lmu.ifi.dbs.knowing.core.graph.xml.DataProcessingUnit;
import de.lmu.ifi.dbs.knowing.core.processing.TLoader;
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

	/* to prevent getNextPage setting input twice */
	private boolean firstcall = true;

	private void initSelections(IPatientService service) {
		/* Can skip sensorPage */
		Patient patient = (Patient) service.getSelection(IPatientService.PATIENT);
		SensorAdapter sensor = (SensorAdapter) service.getSelection(IPatientService.SENSOR);
		if (patient != null && sensor != null)
			sourcePage = new SensorPage(patient, sensor);

	}

	@Override
	public void addPages() {
		IPatientService service = Activator.getPatientService();
		initSelections(service);
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
//		DataProcessingUnit dpu = (DataProcessingUnit) Activator.getPatientService().getSelection(IPatientService.DPU);
		DataProcessingUnit dpu = dpuPage.getSelection();
		if (dpu == null)
			return false;
		
		Patient patient = sourcePage.getPatient();
		SensorAdapter sensor = sourcePage.getSensor();
		IConverter converter = sensor.getSensorExtension().getConverter();
		Date from = dataPage.getFrom();
		Date to = dataPage.getTo();
		
		String dir = getPatientFolder(patient);
		String path = dir + ".tmp-sensor-data";
		
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
		Node[] nodes = dpu.node("loader", converter.getId());
		System.err.println("Nodes: " + nodes);
		for (Node node : nodes) {
			Properties properties = node.properties();
			System.err.println("Properties before: " + properties);
			//TODO QuickAnalyseWizard -> node property TLoader.FILE 
			properties.setProperty("file", path);
			System.err.println("Properties after: " + properties);
			System.err.println("Properties reference: " + node.properties());
		}
		EvaluateHandler.evaluate(dpu);
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
