package de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import de.lmu.ifi.dbs.medmon.base.ui.util.JFaceUtil;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.IValidationPage;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.ValidationListener;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.database.entity.Therapy;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;

public class TherapyResultTherapyPage extends WizardPage implements IValidationPage {

	private Patient				patient;
	private Therapy				selectedTherapy;

	private SortedSet<String>	errors						= new TreeSet<String>();
	private static String		ERROR_NO_THERAPY_SELECTED	= "Keine Therapie ausgew�hlt";

	private TableViewer			tableViewer;
	private TableViewerColumn	clmTherapy;
	private Table				table;

	/**
	 * Create the wizard.
	 */
	public TherapyResultTherapyPage() {
		super("wizardPage");
		setTitle("Therapie ausw�hlen");
		setDescription("<missing>");
	}

	/**
	 * WIZZARD-GET: get selected Therapy
	 */
	public Therapy getSelectedTherapy() {
		return selectedTherapy;
	}

	/**
	 * WIZZARD-SET: set the patient
	 */
	@SuppressWarnings("unchecked")
	public void setPatient(Patient patient) {
		this.patient = patient;

		EntityManager entityManager = JPAUtil.createEntityManager();
		Query allTherapiesQuery = entityManager.createNamedQuery("Therapy.findByPatientId");
		List<Therapy> allTherapies = allTherapiesQuery.setParameter("patientId", patient).getResultList();
		entityManager.close();

		tableViewer.setInput(allTherapies);
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	@SuppressWarnings("unchecked")
	public void createControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		container.setLayout(new GridLayout(1, false));

		tableViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		clmTherapy = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn clmFile = clmTherapy.getColumn();
		clmFile.setResizable(false);
		clmFile.setWidth(300);
		clmFile.setText("Therapie");

		table.addSelectionListener(new ValidationListener(this) {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				if (!selection.isEmpty())
					selectedTherapy = (Therapy) selection.getFirstElement();
				else
					selectedTherapy = null;
				super.widgetSelected(e);
			}
		});
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		clmTherapy.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				cell.setText(((Therapy) cell.getElement()).getCaption());
			}
		});

		checkContents();
	}

	@Override
	public void checkContents() {

		selectedTherapy = JFaceUtil.initializeViewerSelection(Therapy.class, tableViewer);

		if (selectedTherapy == null)
			errors.add(ERROR_NO_THERAPY_SELECTED);
		else
			errors.remove(ERROR_NO_THERAPY_SELECTED);

		if (errors.isEmpty()) {
			setErrorMessage(null);
			setPageComplete(true);
		} else {
			setErrorMessage(errors.first());
			setPageComplete(false);
		}

	}
}
