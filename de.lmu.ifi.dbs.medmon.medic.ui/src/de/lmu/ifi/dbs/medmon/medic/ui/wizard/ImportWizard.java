package de.lmu.ifi.dbs.medmon.medic.ui.wizard;

import java.io.IOException;
import java.util.Date;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.base.ui.wizard.pages.SelectDataPage;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.SensorPage;

public class ImportWizard extends Wizard {

	private final Logger log = LoggerFactory.getLogger(Activator.PLUGIN_ID);
	
	private SensorPage sourcePage;
	private SelectDataPage dataPage;

	//private SensorAdapter sensor;
	private Patient patient;

	public ImportWizard() {
		setWindowTitle("Datenimport");
	}

	public ImportWizard(Object sensor, Patient patient) {
		log.debug("ImportWizard::ImportWizard()");
		//this();
		//this.sensor = sensor;
		//this.patient = patient;
	}

	@Override
	public void addPages() {
		log.debug("ImportWizard::addPages()");
		/*
		if (patient == null && sensor == null) {
			sourcePage = new SensorPage();
			addPage(sourcePage);
		}

		dataPage = new SelectDataPage(true);
		addPage(dataPage);
		*/
	}
	
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		log.debug("ImportWizard::getNextPage()");
		/*
		if(page == sourcePage) {
			dataPage.setSensor(sourcePage.getSensor());
			dataPage.setPatient(sourcePage.getPatient());
			dataPage.checkContents();
		}
		return super.getNextPage(page);
		*/
		return null;
	}

	@Override
	public boolean performFinish() {
		log.debug("ImportWizard::performFinish()");
		/*
		if (sensor == null)
			sensor = sourcePage.getSensor();
		if (patient == null)
			patient = sourcePage.getPatient();
		if(dataPage.isImportLatest()) {
			try {
				Date from = dataPage.getFrom();
				Date to = dataPage.getTo();
				
				//Store on filesystem
				String extension = sensor.getSensorExtension().getConverter().getFileExtension();
				String path = createOutputPath(from, to, extension);
				sensor.copy(new FileOutputStream(path));
				
				//Store in db
				EntityManager em = JPAUtil.createEntityManager();
				em.getTransaction().begin();
				Data data = new Data();
				data.setPatient(patient);
				data.setSensor(sensor.getSensorEntity());
				data.setFile(path);
				data.setFrom(from);
				data.setTo(to);
				em.persist(data);
				em.getTransaction().commit();
			} catch (IOException e) {
				MessageDialog.openError(getShell(), "Fehler beim Daten importieren", e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
				MessageDialog.openError(getShell(), "Unbekannter Fehler speichern der Sensordaten", e.getMessage());
				e.printStackTrace();
			}
		}
		*/
		// Set the global selection
//		Activator.getPatientService().setSelection(root, IPatientService.SENSOR_CONTAINER);
		// Persist
/*		if (dataPage.isPersist()) {
			if (children.length == 0) {
				MessageDialog.openError(getShell(), "Keine Daten", "Sie muessen Daten auswaehlen");
				return false;
			}

			ISensorDataContainer c = children[0];
			String file = (String) c.getBlock().getDescriptor().getAttribute(BlockDescriptorOld.FILE);
			file = moveSensorFile(file, patient);
			new PersistJob("Daten in Datenbank speichern", file, root, sensor, patient).schedule();
		}*/

		//return true;
		return false;
	}
	
	private String createOutputPath(Date from, Date to, String extension) throws IOException {
		log.warn("USE IPatientService here!");
		return "empty";
	}

}
