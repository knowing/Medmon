package de.lmu.ifi.dbs.medmon.medic.core.util;

import java.io.File;

import org.eclipse.jface.preference.IPreferenceStore;

import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.Activator;
import de.lmu.ifi.dbs.medmon.medic.core.preferences.IMedicPreferences;

/**
 * 
 * @author Nepomuk Seiler
 * 
 */
public class ApplicationConfigurationUtil {
	
	/* =========================== */
	/* == Patient configuration == */
	/* =========================== */
	
	/**
	 * Creates if not exists and returns path to patient-folder
	 * @param patient
	 * @return path to patient folder: {user.home}/.medmon/{id}-lastname/
	 */
	public static String getPatientFolder(Patient patient) {
		String path = getPreferenceStore().getString(IMedicPreferences.DIR_PATIENT_ID);
		String sep  = getPreferenceStore().getString(IMedicPreferences.DIR_SEPERATOR_ID);
		
		StringBuffer sb = new StringBuffer();
		sb.append(path);
		sb.append(sep);
		sb.append(String.format("%011d", patient.getId()));
		sb.append("-");
		sb.append(patient.getLastname());
		sb.append(sep);
		String folder = sb.toString();
		
		//Create if not exists
		File patientFolder = new File(folder);
		if(!patientFolder.exists() || !patientFolder.isDirectory()) {
			patientFolder.delete();
			patientFolder.mkdirs();
			new File(patientFolder + sep + "data").mkdir();
			new File(patientFolder + sep + "cluster").mkdir();
		}
		return folder;
		
	}
	
	/**
	 * 
	 * @param patient
	 */
	public static void createPatientFolder(Patient patient)  {
		String sep  = getPreferenceStore().getString(IMedicPreferences.DIR_SEPERATOR_ID);
		
		StringBuffer sb = new StringBuffer();
		sb.append(getPatientFolder(patient));
		File patient_dir = new File(sb.toString());
		if(patient_dir.mkdirs()) {
			if(!patient_dir.isDirectory()) {
				if(patient_dir.delete())
					patient_dir.mkdirs();
			}
		}
		sb.append(sep);
		new File(sb.toString() + "cluster").mkdir();
		new File(sb.toString() + "data").mkdir();
		
	}
	
	/* =========================== */
	/* ====== Patient data ======= */
	/* =========================== */
	
	@Deprecated
	public static String createClusterUnitFile(Object cu, Patient patient) {
		StringBuffer sb = new StringBuffer();
		sb.append(getPatientFolder(patient));
		sb.append("cluster");
		sb.append(getPreferenceStore().getString(IMedicPreferences.DIR_SEPERATOR_ID));
//		sb.append(cu.getName());	
		sb.append(".xml");
		return sb.toString();
	}
	
	@Deprecated
	public static String getClusterUnitFolder(Patient patient) {
		String sep = getPreferenceStore().getString(IMedicPreferences.DIR_SEPERATOR_ID);
		String patientFolder = getPatientFolder(patient);
		return patientFolder  + "cluster" + sep;
	}
		
	/* =========================== */
	/* == Data Processing Unit === */
	/* =========================== */

	
	
	/* =========================== */
	/* == General configuration == */
	/* =========================== */
	
	
	public static IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}
}
