package de.lmu.ifi.dbs.medmon.base.ui.wizard.pages;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import de.lmu.ifi.dbs.medmon.base.ui.util.JFaceUtil;
import de.lmu.ifi.dbs.medmon.base.ui.viewer.SensorTableViewer;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.IValidationPage;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.ValidationListener;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

public class ImportDataSensorAndDirectoryPage extends WizardPage implements IValidationPage {

	private static String		ERROR_NO_DIRECTORY_CHOOSEN	= "Keine Verzeichnis ausgew\u00e4hlt";
	private static String		ERROR_NO_SENSOR_SELECTED	= "Kein Sensor ausgew\u00e4hlt";

	private ISensor				selectedSensor				= null;
	private boolean				isDirectorySectionEnabled	= false;
	private SortedSet<String>	errors						= new TreeSet<String>();

	private Button				btnChooseDirectory;
	private SensorTableViewer	sensorTableViewer;
	private Text				txtSelectedDirectory;

	/**
	 * Create the wizard.
	 */
	public ImportDataSensorAndDirectoryPage() {
		super("ImportDataSensorAndDirectoryPage");
		setTitle("Sensor ausw\u00e4hlen");
		setDescription("W\u00e4hlen Sie einen Sensor aus von dem Sie die Daten verwenden m\u00f6chten.");
	}

	/**
	 * Create contents of the wizard.
	 */
	public void createControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		container.setLayout(new GridLayout(2, false));

		sensorTableViewer = new SensorTableViewer(container, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
		Table table = sensorTableViewer.getTable();
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

		txtSelectedDirectory = new Text(container, SWT.BORDER);
		txtSelectedDirectory.setEditable(false);
		txtSelectedDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		btnChooseDirectory = new Button(container, SWT.NONE);
		btnChooseDirectory.setText("Durchsuchen");
		btnChooseDirectory.addSelectionListener(new ValidationListener(this) {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(getShell());
				dlg.setText("Standard-Pfad");
				String selectedDirectory = dlg.open();
				if (selectedDirectory != null)
					txtSelectedDirectory.setText(selectedDirectory);
				else
					txtSelectedDirectory.setText("");

				checkContents();
			}
		});

		checkContents();
	}

	@Override
	public void checkContents() {

		selectedSensor = JFaceUtil.initializeViewerSelection(ISensor.class, sensorTableViewer);

		if (isDirectorySectionEnabled) {
			if (txtSelectedDirectory.getText() == null || txtSelectedDirectory.getText().isEmpty())
				errors.add(ERROR_NO_DIRECTORY_CHOOSEN);
			else
				errors.remove(ERROR_NO_DIRECTORY_CHOOSEN);
		} else
			errors.remove(ERROR_NO_DIRECTORY_CHOOSEN);

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
		return txtSelectedDirectory.getText();
	}

	/**
	 * WIZZARD-SET: set true if you need to select a directory
	 */
	public void setDirectorySectionEnabled(boolean flag) {
		isDirectorySectionEnabled = flag;
		btnChooseDirectory.setEnabled(flag);
	}
}
