package de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.handlers.HandlerUtil;

import de.lmu.ifi.dbs.medmon.base.ui.dialog.DialogFactory;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportDataWizard;
import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizardOptions.*;

public class ImportDataPatientAndTypePage extends WizardPage {
	private Text	textLastname;
	private Text	textFirstname;
	private int		option	= 0;

	private Button	btnRawData;
	private Button	btnSensor;
	private Button	btnFile;
	private Button	btnTrainingData;
	private Button	btnCSV;
	private Button	btnARFF;

	/**
	 * Create the wizard.
	 */
	public ImportDataPatientAndTypePage() {
		super("wizardPage");
		setTitle("Daten importieren");
		setDescription("<missing>");
	}

	public int getOption() {
		return option;
	}

	private void refreshOption() {
		option = NO_OPTION;
		if (btnRawData.getSelection())
			option = option | IMPORT_RAW;
		if (btnTrainingData.getSelection())
			option = option | IMPORT_TRAINING;
		if (btnSensor.getSelection())
			option = option | SOURCE_SENSOR;
		if (btnFile.getSelection())
			option = option | SOURCE_SENSOR;
		if (btnCSV.getSelection())
			option = option | TYPE_CSV;
		if (btnARFF.getSelection())
			option = option | TYPE_ARFF;
	}

	private void initialize() {

		setPageComplete(false);
		refreshOption();

		IGlobalSelectionProvider selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
		Patient selectedPatient = selectionProvider.getSelection(Patient.class);

		if (selectedPatient == null) {
			MessageDialog.openInformation(getShell(), "Information", "Bitte wählen sie zuerst einen Patienten aus!");
			selectPatient(DialogFactory.openPatientSelectionDialog(getShell()));
		} else {
			selectPatient(selectedPatient);
		}
	}

	private void selectPatient(Patient patient) {
		if (patient == null) {
			setPageComplete(false);
			textFirstname.setText("");
			textLastname.setText("");
		} else {
			textFirstname.setText(patient.getFirstname());
			textLastname.setText(patient.getLastname());
			ImportDataWizard wizard = (ImportDataWizard) getWizard();
			wizard.setSelectedPatient(patient);
			setPageComplete(true);
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
		btnChoosePatient.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectPatient(DialogFactory.openPatientSelectionDialog(getShell()));
			}
		});

		Group grpDataType = new Group(container, SWT.NONE);
		grpDataType.setText("Datentyp:");
		grpDataType.setLayout(new GridLayout(1, false));
		grpDataType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 5, 1));

		btnRawData = new Button(grpDataType, SWT.RADIO);
		btnRawData.setSelection(true);
		btnRawData.setText("Rohdaten");
		btnRawData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnSensor.setEnabled(true);
				btnFile.setEnabled(true);
				btnCSV.setEnabled(false);
				btnARFF.setEnabled(false);
				refreshOption();
			}
		});

		Group grpSource = new Group(grpDataType, SWT.NONE);
		grpSource.setText("Quelle:");
		grpSource.setLayout(new GridLayout(1, false));

		btnSensor = new Button(grpSource, SWT.RADIO);
		btnSensor.setSelection(true);
		btnSensor.setText("Sensor - importiert Daten von einem angeschlossenem Sensor");

		btnFile = new Button(grpSource, SWT.RADIO);
		btnFile.setEnabled(false);
		btnFile.setText("Datei - importiert die Daten aus einer Datei auf diesem Computer");

		btnTrainingData = new Button(grpDataType, SWT.RADIO);
		btnTrainingData.setText("Trainingsdaten");
		btnTrainingData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnSensor.setEnabled(false);
				btnFile.setEnabled(false);
				btnCSV.setEnabled(true);
				btnARFF.setEnabled(true);
				refreshOption();
			}
		});

		Group grpType = new Group(grpDataType, SWT.NONE);
		grpType.setText("Typ:");
		grpType.setLayout(new GridLayout(1, false));

		btnCSV = new Button(grpType, SWT.RADIO);
		btnCSV.setEnabled(false);
		btnCSV.setSelection(true);
		btnCSV.setText("CSV - importiert die Daten aus einer CSV datei");

		btnARFF = new Button(grpType, SWT.RADIO);
		btnARFF.setEnabled(false);
		btnARFF.setText("ARFF - importiert die Daten aus einer ARFF datei");

		initialize();
	}
}
