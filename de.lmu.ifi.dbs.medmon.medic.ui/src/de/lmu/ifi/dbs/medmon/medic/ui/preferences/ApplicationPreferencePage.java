package de.lmu.ifi.dbs.medmon.medic.ui.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.lmu.ifi.dbs.medmon.medic.core.preferences.IMedicPreferences;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;

/**
 * 
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 2011
 */
public class ApplicationPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Create the preference page.
	 */
	public ApplicationPreferencePage() {
		super(GRID);
	}

	/**
	 * Create contents of the preference page.
	 */
	@Override
	protected void createFieldEditors() {
		addField(new DirectoryFieldEditor(IMedicPreferences.MEDMON_DIR, "Programmordner", getFieldEditorParent()));
	}
	
	@Override
	protected void checkState() {
		super.checkState();
		String defaultValue = getPreferenceStore().getDefaultString(IMedicPreferences.MEDMON_DIR);
		String value = getPreferenceStore().getString(IMedicPreferences.MEDMON_DIR);
		if(!defaultValue.equals(value)) {
			setErrorMessage("Sie duerfen das Programmverzeichnis nicht aendern");
			setValid(false);
		}
			
	}
	
	/**
	 * Initialize the preference page.
	 */
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Programmeinstellungen");
	}

}
