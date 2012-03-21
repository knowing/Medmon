package de.lmu.ifi.dbs.medmon.medic.core.preferences;

import java.util.Arrays;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

public class MedmonPreferenceInitializer extends AbstractPreferenceInitializer implements IMedicPreferences {

	private static final String DIR_USER_HOME = System.getProperty("user.home");
	private static final String DIR_SEPERATOR = System.getProperty("file.separator");
	private static final String DIR_MEDMON = DIR_USER_HOME + DIR_SEPERATOR + ".medmon";
	
	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences medmonNode = ConfigurationScope.INSTANCE.getNode(MEDMON_NODE);
		
		medmonNode.put(MEDMON_DIR, DIR_MEDMON);
		medmonNode.put(MEDMON_DPU, DIR_MEDMON + DIR_SEPERATOR + "dpu");
		medmonNode.put(MEDMON_PATIENT, DIR_MEDMON + DIR_SEPERATOR + "patients");
		medmonNode.put(MEDMON_TMP, DIR_MEDMON + DIR_SEPERATOR + ".tmp");
		try {
			medmonNode.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		
		//Test
		System.err.println("Node: " + medmonNode);
		try {
			System.err.println("Keys: " + Arrays.toString(medmonNode.keys()));
			System.err.println("Children: " + Arrays.toString(medmonNode.childrenNames()));
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		
		IEclipsePreferences dbNode = ConfigurationScope.INSTANCE.getNode(DATABASE_NODE);
		dbNode.put(DATABASE_DIR, DIR_MEDMON + DIR_SEPERATOR + "db");
	}	

}
