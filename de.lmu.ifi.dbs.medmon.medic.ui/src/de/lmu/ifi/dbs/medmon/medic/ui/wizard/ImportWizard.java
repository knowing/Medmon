package de.lmu.ifi.dbs.medmon.medic.ui.wizard;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.EntityManager;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import de.lmu.ifi.dbs.medmon.base.ui.wizard.pages.SelectDataPage;
import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.medic.core.sensor.SensorAdapter;
import de.lmu.ifi.dbs.medmon.medic.core.util.ApplicationConfigurationUtil;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.SensorPage;

public class ImportWizard extends Wizard {

	private SensorPage sourcePage;
	private SelectDataPage dataPage;

	private SensorAdapter sensor;
	private Patient patient;

	public ImportWizard() {
		setWindowTitle("Datenimport");
	}

	public ImportWizard(SensorAdapter sensor, Patient patient) {
		this();
		this.sensor = sensor;
		this.patient = patient;
	}

	@Override
	public void addPages() {
		if (patient == null && sensor == null) {
			sourcePage = new SensorPage();
			addPage(sourcePage);
		}

		dataPage = new SelectDataPage();
		addPage(dataPage);
	}
	
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if(page == sourcePage) {
			dataPage.setSensor(sourcePage.getSensor());
			dataPage.setPatient(sourcePage.getPatient());
		}
		return super.getNextPage(page);
	}

	@Override
	public boolean performFinish() {
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

		return true;
	}
	
	private String createOutputPath(Date from, Date to, String extension) throws IOException {
		StringBuilder sb = new StringBuilder();
		String sep = System.getProperty("file.separator");
		
		sb.append(ApplicationConfigurationUtil.getPatientFolder(patient));
		sb.append("data");
		sb.append(sep);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		sb.append(df.format(from));
		sb.append("_");
		sb.append(df.format(to));
		sb.append(extension);
		return sb.toString();
	}

}
