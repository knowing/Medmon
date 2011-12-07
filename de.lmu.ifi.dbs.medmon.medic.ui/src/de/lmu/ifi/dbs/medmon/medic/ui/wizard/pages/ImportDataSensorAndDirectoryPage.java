package de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizardOptions.*;

import de.lmu.ifi.dbs.medmon.base.ui.viewer.SensorTableViewer;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.IValidationPage;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.ValidationListener;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportDataWizard;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

public class ImportDataSensorAndDirectoryPage extends WizardPage implements IValidationPage {
	private Table				table;
	private Text				textFile;
	private String				selectedDirectory			= null;
	private ISensor				selectedSensor				= null;
	private boolean				isDirectorySectionEnabled	= false;
	private SortedSet<String>	errors						= new TreeSet<String>();

	private static String		ERROR_NO_FILE_CHOOSEN		= "Keine datei ausgewählt";
	private static String		ERROR_NO_SENSOR_SELECTED	= "Kein Sensor ausgewählt";
	private Button				btnChooseFile;

	/**
	 * Create the wizard.
	 */
	public ImportDataSensorAndDirectoryPage() {
		super("wizardPage");
		setTitle("Sensor ausw\u00E4hlen");
		setDescription("<missing>");
	}

	/**
	 * WIZZARD-GET: get selected Sensor
	 */
	public ISensor getSelectedSensor() {
		return selectedSensor;
	}

	/**
	 * WIZZARD-GET: get selected Directory
	 */
	public String getSelectedDirectory() {
		return selectedDirectory;
	}

	/**
	 * WIZZARD-SET: set true if you need to select a directory
	 */
	public void setDirectorySectionEnabled(boolean flag) {
		isDirectorySectionEnabled = flag;
		btnChooseFile.setEnabled(flag);
	}

	/**
	 * WIZZARD-INIT
	 */
	private void initialize() {
		checkContents();
	}

	/**
	 * Create contents of the wizard.
	 */
	public void createControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		container.setLayout(new GridLayout(2, false));

		final SensorTableViewer sensorTableViewer = new SensorTableViewer(container, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
		// final TableViewer sensorTableViewer = new TableViewer(container);
		table = sensorTableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		table.addSelectionListener(new ValidationListener(this) {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) sensorTableViewer.getSelection();
				if (!selection.isEmpty())
					selectedSensor = (ISensor) selection.getFirstElement();
				else
					selectedSensor = null;
				super.widgetSelected(e);
			}
		});

		textFile = new Text(container, SWT.BORDER);
		textFile.setEditable(false);
		textFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		btnChooseFile = new Button(container, SWT.NONE);
		btnChooseFile.setText("Datei");
		btnChooseFile.addSelectionListener(new ValidationListener(this) {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(getShell());
				dlg.setText("Standard-Pfad");
				selectedDirectory = dlg.open();
				if (selectedDirectory != null)
					textFile.setText(selectedDirectory);
				else
					textFile.setText("");
				super.widgetSelected(e);
			}
		});

		initialize();
	}

	@Override
	public void checkContents() {
		
		if (isDirectorySectionEnabled) {
			if (selectedDirectory == null)
				errors.add(ERROR_NO_FILE_CHOOSEN);
			else
				errors.remove(ERROR_NO_FILE_CHOOSEN);
		} else
			errors.remove(ERROR_NO_FILE_CHOOSEN);

		if (selectedSensor == null)
			errors.add(ERROR_NO_SENSOR_SELECTED);
		else
			errors.remove(ERROR_NO_SENSOR_SELECTED);

		if (errors.isEmpty()) {
			setErrorMessage(null);
			setPageComplete(true);
		} else {
			setErrorMessage(errors.first());
			setPageComplete(false);
		}

	}
}
