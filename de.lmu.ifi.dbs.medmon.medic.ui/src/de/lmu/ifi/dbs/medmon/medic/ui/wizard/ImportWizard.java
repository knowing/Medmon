package de.lmu.ifi.dbs.medmon.medic.ui.wizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import de.lmu.ifi.dbs.medmon.base.ui.wizard.pages.SelectDataPage;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
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
		if(page == sourcePage)
			dataPage.setSensor(sourcePage.getSensor());
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
				String extension = sensor.getSensorExtension().getConverter().getFileExtension();
				sensor.copy(createOutputFile(from, to, extension));
				
			} catch (IOException e) {
				MessageDialog.openError(getShell(), "Failed to import data", e.getMessage());
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
	
	private OutputStream createOutputFile(Date from, Date to, String extension) throws IOException {
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
		return new FileOutputStream(sb.toString());
	}

	private String moveSensorFile(String oldFilePath, Patient patient) {
		String sep = System.getProperty("file.separator");
		String returns = ApplicationConfigurationUtil.getPatientFolder(patient);
		returns += "data" + sep;
		String name = oldFilePath.substring(oldFilePath.lastIndexOf(sep) + 1);
		// DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
		// DateFormat.SHORT);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd_hh:mm");
		returns += df.format(new Date()) + "-" + name;

		File in = new File(oldFilePath);
		File out = new File(returns);
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		try {
			out.createNewFile();

			inChannel = new FileInputStream(in).getChannel();
			outChannel = new FileOutputStream(out).getChannel();
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (inChannel != null)
					inChannel.close();
				if (outChannel != null)
					outChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return returns;

	}

}
