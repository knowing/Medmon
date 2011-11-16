package de.lmu.ifi.dbs.medmon.medic.ui.wizard;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.Wizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Instances;
import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.SelectDBDataPage;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;
import de.lmu.ifi.dbs.medmon.sensor.core.converter.IConverter;
import de.lmu.ifi.dbs.medmon.sensor.core.util.SensorUtil;

public class DataSelectionWizard extends Wizard {

	private SelectDBDataPage page;
	private static final Logger log = LoggerFactory.getLogger(Activator.PLUGIN_ID);
	
	public DataSelectionWizard() {
		setWindowTitle("Data Selection Wizard");
	}

	@Override
	public void addPages() {
		page = new SelectDBDataPage();
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		EntityManager em = JPAUtil.createEntityManager();
		Patient patient = page.getPatient();
		// Select all days imported
		List<Data> results = em.createNamedQuery("Data.findByPatient", Data.class)
				.setParameter("patient", patient).getResultList();
		em.close();
		if (results.isEmpty()) {
			page.setErrorMessage("Keine Daten vom Patienten vorhanden");
			return true;
		}

		Sensor dbsensor = results.get(0).getSensor();
		List<ISensor> sensors = SensorUtil.evaluateExtensionsAsList();
		ISensor sensor = null;
		for (Object o : sensors) {
			ISensor s = (ISensor) o;
			String id = Sensor.parseId(s.getName(), s.getVersion());
			if (id.equals(dbsensor.getId())) {
				sensor = s;
				break;
			}
		}
		if (sensor == null) {
			page.setErrorMessage("Der Sensor " + dbsensor.getName() + " ist nicht installiert");
			return false;
		}

		// The new input for the Viewer
		
		log.debug("IConverter converter = sensor.getConverter();");
		IConverter converter = null;
		for (Data data : results) {
			//				ISensorDataContainer c = converter.convertToContainer(data.getFile(), ContainerType.WEEK, ContainerType.HOUR, null);
			//				if(c instanceof RootSensorDataContainer) 
			//					((RootSensorDataContainer)c).setName(data.getFile());
			//				root.addChild(c);
							System.err.println("NOT IMPLEMENTED YET: " + getClass().getName());
							log.debug("Instances dataSet = converter.getDataSet();");
							//Instances dataSet = converter.getDataSet();
							Instances dataSet = null;
		}

//		Activator.getPatientService().setSelection(root, IPatientService.SENSOR_CONTAINER);
		return true;
	}

}
