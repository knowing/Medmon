package de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.lmu.ifi.dbs.medmon.base.ui.dialog.DialogFactory;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.TherapyResultWizard;

public class TherapyResultPatientPage extends WizardPage {

	private Text	textLastname;
	private Text	textFirstname;
	private Patient patient;

	/**
	 * Create the wizard.
	 */
	public TherapyResultPatientPage() {
		super("wizardPage");
		setPageComplete(false);
		setTitle("Wizard Page title");
		setDescription("Wizard Page description");
	}

	private void selectPatient(Patient patient) {
		if (patient == null) {
			setPageComplete(false);
			textFirstname.setText("");
			textLastname.setText("");
		} else {
			textFirstname.setText(patient.getFirstname());
			textLastname.setText(patient.getLastname());
			this.patient = patient;
			setPageComplete(true);
		}
	}

	private void initialize() {
		IGlobalSelectionProvider selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
		Patient selectedPatient = selectionProvider.getSelection(Patient.class);
		selectPatient(selectedPatient);
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

		textFirstname = new Text(container, SWT.BORDER);
		textFirstname.setEditable(false);
		textFirstname.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblFirstName = new Label(container, SWT.NONE);
		lblFirstName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFirstName.setText("Nachname:");

		textLastname = new Text(container, SWT.BORDER);
		textLastname.setEditable(false);
		textLastname.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btnChoosePatient = new Button(container, SWT.NONE);
		btnChoosePatient.setText("ausw\u00E4hlen");
		btnChoosePatient.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectPatient(DialogFactory.openPatientSelectionDialog(getShell()));
			}
		});

		selectPatient(null);
		initialize();
	}
	
	public Patient getPatient() {
		return patient;
	}
}
