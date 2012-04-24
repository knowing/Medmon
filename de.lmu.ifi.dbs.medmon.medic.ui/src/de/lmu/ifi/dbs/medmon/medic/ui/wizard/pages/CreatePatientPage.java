package de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.lmu.ifi.dbs.medmon.base.ui.wizard.IValidationPage;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.ValidationListener;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.provider.ISharedImages;

public class CreatePatientPage extends WizardPage implements IValidationPage {

	private static final String	MALE_STRING						= "Maennlich";
	private static final String	FEMAL_STRING					= "Weiblich";

	// User can choose next, but not finish
	private boolean				flip							= false;

	private Text				tFirstname;
	private Text				tLastname;
	private Text				tInsuranceId;

	private CDateTime			dBirth;
	private ComboViewer			genderViewer;

	// Errors
	/** The <code>Stack</code> for errors occured in this page. */
	private SortedSet<String>	errors;

	private static final String	ERROR_IDENTICAL_PATIENT			= "Identischer Patient vorhanden";
	private static final String	ERROR_IDENTICAL_INSURANCE_ID	= "Identische Versicherungsnummer";
	private static final String	ERROR_NO_INSURANCE_ID			= "Keine Versicherungsnummer";

	public CreatePatientPage() {
		super("CreatePatientPage");
		setDescription("Bitte w\u00e4hlen sie einen Patienten aus.");
		setTitle("Patient ausw\u00e4hlen");
		setImageDescriptor(Activator.getImageDescriptor(ISharedImages.IMG_ADD_PATIENT_48));
		errors = new TreeSet<String>();
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(4, false));

		setControl(container);

		Label lFirstname = new Label(container, SWT.NONE);
		lFirstname.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lFirstname.setText("Vorname");

		tFirstname = new Text(container, SWT.BORDER);
		GridData gd_tFirstname = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_tFirstname.widthHint = 150;
		tFirstname.setLayoutData(gd_tFirstname);
		tFirstname.addModifyListener(new ValidationListener(this));

		Label lLastname = new Label(container, SWT.NONE);
		lLastname.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lLastname.setText("Nachname");

		tLastname = new Text(container, SWT.BORDER);
		GridData gd_tLastname = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_tLastname.widthHint = 150;
		tLastname.setLayoutData(gd_tLastname);
		tLastname.addModifyListener(new ValidationListener(this));

		Label lBirth = new Label(container, SWT.NONE);
		lBirth.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lBirth.setText("Geburtsdatum");

		dBirth = new CDateTime(container, CDT.BORDER | CDT.SPINNER);
		dBirth.addSelectionListener(new ValidationListener(this));

		Label lGender = new Label(container, SWT.NONE);
		lGender.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lGender.setText("Geschlecht");

		genderViewer = new ComboViewer(container, SWT.NONE);
		Combo cGender = genderViewer.getCombo();
		GridData gd_cGender = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_cGender.widthHint = 160;
		cGender.setLayoutData(gd_cGender);
		genderViewer.setContentProvider(new ArrayContentProvider());
		genderViewer.setInput(new String[] { MALE_STRING, FEMAL_STRING });

		Label lSocialnumber = new Label(container, SWT.NONE);
		lSocialnumber.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lSocialnumber.setText("Versicherungsnummer");

		tInsuranceId = new Text(container, SWT.BORDER);
		tInsuranceId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tInsuranceId.addModifyListener(new ValidationListener(this));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		setPageComplete(false);
	}

	@Override
	public boolean canFlipToNextPage() {
		return flip;
	}

	@Override
	public void checkContents() {
		// Check identical patient
		EntityManager em = JPAUtil.createEntityManager();
		List resultList = em.createNamedQuery("Patient.findIdentical").setParameter("firstname", tFirstname.getText())
				.setParameter("lastname", tLastname.getText()).setParameter("birth", dBirth.getSelection()).getResultList();

		if (!resultList.isEmpty())
			errors.add(ERROR_IDENTICAL_PATIENT);
		else
			errors.remove(ERROR_IDENTICAL_PATIENT);

		// Check InsuranceNumber empty
		if (tInsuranceId.getText().isEmpty()) {
			errors.add(ERROR_NO_INSURANCE_ID);
		} else {
			errors.remove(ERROR_NO_INSURANCE_ID);
		}

		// Check InsuranceNumber
		resultList = em.createNamedQuery("Patient.findByInsuranceId").setParameter("insuranceId", tInsuranceId.getText()).getResultList();
		if (!resultList.isEmpty())
			errors.add(ERROR_IDENTICAL_INSURANCE_ID);
		else
			errors.remove(ERROR_IDENTICAL_INSURANCE_ID);

		if (errors.isEmpty()) {
			setErrorMessage(null);
			flip = true;
			setPageComplete(true);
		} else {
			setErrorMessage(errors.first());
			flip = false;
			setPageComplete(false);
		}
		em.close();
	}

	public void initializePatient(Patient patient) {
		patient.setFirstname(tFirstname.getText());
		patient.setLastname(tLastname.getText());
		patient.setBirth(dBirth.getSelection());
		patient.setGender(getGender());
		patient.setInsuranceId(tInsuranceId.getText());
	}

	private short getGender() {
		IStructuredSelection selection = (IStructuredSelection) genderViewer.getSelection();
		if (selection.isEmpty())
			return Patient.MALE;
		String element = (String) selection.getFirstElement();
		if (element.equals(MALE_STRING))
			return Patient.MALE;
		return Patient.FEMALE;
	}

}
