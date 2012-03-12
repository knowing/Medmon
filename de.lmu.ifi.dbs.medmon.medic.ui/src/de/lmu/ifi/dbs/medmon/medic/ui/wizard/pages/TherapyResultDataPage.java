package de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages;

import java.util.List;

import javax.persistence.EntityManager;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import de.lmu.ifi.dbs.medmon.base.ui.provider.WorkbenchTableLabelProvider;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionListener;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;

public class TherapyResultDataPage extends WizardPage {

	IGlobalSelectionProvider	selectionProvider	= GlobalSelectionProvider.newInstance(Activator.getBundleContext());
	private Table				table;
	private TableViewer			tableViewer;

	/**
	 * Create the wizard.
	 */
	public TherapyResultDataPage() {
		super("wizardPage");
		setPageComplete(false);
		setTitle("Therapie ausw\u00E4hlen");
		setDescription("w\u00E4hlen sie die Therapie aus, zu der das neue Ergebnis geh\u00F6ren soll");
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		container.setLayout(new GridLayout(1, false));

		tableViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnNewColumn = tableViewerColumn.getColumn();
		tblclmnNewColumn.setWidth(100);
		tblclmnNewColumn.setText("Therapie");

		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnNewColumn_1 = tableViewerColumn_1.getColumn();
		tblclmnNewColumn_1.setWidth(450);
		tblclmnNewColumn_1.setText("Kommentar");

		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new WorkbenchTableLabelProvider());

		selectionProvider.registerSelectionListener(new IGlobalSelectionListener<Patient>() {
			@Override
			public void selectionChanged(Patient selection) {
				if (selection == null)
					return;
				EntityManager entityManager = Activator.getEntityManagerService().createEntityManager();
				@SuppressWarnings("unchecked")
				List<Sensor> results = entityManager.createNamedQuery("Therapy.findByPatientId")
						.setParameter("patientId", selection.getId()).getResultList();
				tableViewer.setInput(results);
			}

			@Override
			public void selectionUpdated() {
			}

			@Override
			public Class<Patient> getType() {
				return null;
			}
		});
	}


	@Override
	public void dispose() {
		selectionProvider.unregister();
		super.dispose();
	}
}
