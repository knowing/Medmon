package de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages;

import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizardOptions.IMPORT_RAW;
import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizardOptions.IMPORT_TRAINING;
import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizardOptions.NO_OPTION;
import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizardOptions.PATIENT_SELECTED;
import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizardOptions.SOURCE_SENSOR;
import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizardOptions.SOURCE_FILE;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.lmu.ifi.dbs.medmon.base.ui.dialog.DialogFactory;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.IValidationPage;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.ValidationListener;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportDataWizard;

public class ImportDataPatientAndTypePage extends WizardPage implements IValidationPage {
	private Text				textLastname;
	private Text				textFirstname;
	private int					options						= 0;

	private Button				btnRawData;
	private Button				btnSensor;
	private Button				btnFile;
	private Button				btnTraining;

	private Patient				selectedPatient				= null;

	private SortedSet<String>	errors						= new TreeSet<String>();
	private static String		ERROR_NO_PATIENT_SELECTED	= "Kein Patient ausgewählt";

	/**
	 * Create the wizard.
	 */
	public ImportDataPatientAndTypePage() {
		super("wizardPage");
		setTitle("Daten importieren");
		setDescription("<missing>");
	}

	/**
	 * WIZZARD-GET: get selected Patient 
	 * @return
	 */
	public Patient getSelectedPatient() {
		return selectedPatient;
	}
	
	/**
	 * WIZZARD-GET: get selected Options
	 * @return
	 */
	public int getOption() {
		options = NO_OPTION;

		if (btnRawData.getSelection())
			options = options | IMPORT_RAW;
		if (btnTraining.getSelection())
			options = options | IMPORT_TRAINING;
		if (btnSensor.getSelection())
			options = options | SOURCE_SENSOR;
		if (btnFile.getSelection())
			options = options | SOURCE_FILE;
		if (selectedPatient != null)
			options = options | PATIENT_SELECTED;

		return options;
	}

	/**
	 * WIZZARD-INIT:
	 * @return
	 */
	private void initialize() {

		IGlobalSelectionProvider selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
		Patient selectedPatient = selectionProvider.getSelection(Patient.class);
		selectPatient(selectedPatient);

		checkContents();
	}

	private void selectPatient(Patient patient) {
		selectedPatient = patient;
		if (patient == null) {
			textFirstname.setText("");
			textLastname.setText("");
		} else {
			textFirstname.setText(patient.getFirstname());
			textLastname.setText(patient.getLastname());
		}
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		container.setLayout(new GridLayout(5, false));

		Label lblName = new Label(container, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblName.setText("Vorname:");

		textLastname = new Text(container, SWT.BORDER);
		textLastname.setEditable(false);
		textLastname.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblFirstName = new Label(container, SWT.NONE);
		lblFirstName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFirstName.setText("Nachname:");

		textFirstname = new Text(container, SWT.BORDER);
		textFirstname.setEditable(false);
		textFirstname.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btnChoosePatient = new Button(container, SWT.NONE);
		btnChoosePatient.setText("ausw\u00E4hlen");
		btnChoosePatient.addSelectionListener(new ValidationListener(this) {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectPatient(DialogFactory.openPatientSelectionDialog(getShell()));
				super.widgetSelected(e);
			}
		});

		Group grpDataType = new Group(container, SWT.NONE);
		grpDataType.setText("Datentyp:");
		grpDataType.setLayout(new GridLayout(1, false));
		grpDataType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 5, 1));

		Group grpSource = new Group(grpDataType, SWT.NONE);
		grpSource.setText("Quelle:");
		grpSource.setLayout(new GridLayout(1, false));

		btnSensor = new Button(grpSource, SWT.RADIO);
		btnSensor.setSelection(true);
		btnSensor.setText("Sensor - importiert Daten von einem angeschlossenem Sensor");

		btnFile = new Button(grpSource, SWT.RADIO);
		btnFile.setText("Datei - importiert die Daten aus einer Datei auf diesem Computer");

		Group grpType = new Group(grpDataType, SWT.NONE);
		grpType.setText("Typ:");
		grpType.setLayout(new GridLayout(1, false));

		btnRawData = new Button(grpType, SWT.RADIO);
		btnRawData.setSelection(true);
		btnRawData.setText("Rohdaten");

		btnTraining = new Button(grpType, SWT.RADIO);
		btnTraining.setText("Training");

		initialize();
	}

	@Override
	public void checkContents() {

		if (selectedPatient == null)
			errors.add(ERROR_NO_PATIENT_SELECTED);
		else
			errors.remove(ERROR_NO_PATIENT_SELECTED);

		if (errors.isEmpty()) {
			setErrorMessage(null);
			setPageComplete(true);
		} else {
			setErrorMessage(errors.first());
			setPageComplete(false);
		}
	}
}
