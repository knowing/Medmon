package de.lmu.ifi.dbs.medmon.medic.ui.views;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Therapy;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionListener;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;

public class PatientFileDetailBlock extends MasterDetailsBlock {

	private FormToolkit toolkit;
	private Text text;
	private ListViewer therapiesViewer;
	private Link linkAdd;
	private Link linkRemove;

	private EntityManager entityManager;
	private Patient localPatientSelection;
	private Therapy localTherapySelection;
	private GlobalSelectionProvider provider;

	/**
	 * Create the master details block.
	 */
	public PatientFileDetailBlock() {
		entityManager = JPAUtil.createEntityManager();
		provider = new GlobalSelectionProvider();
	}

	/**
	 * Create contents of the master details block.
	 * 
	 * @param managedForm
	 * @param parent
	 */
	@Override
	protected void createMasterPart(final IManagedForm managedForm, Composite parent) {
		toolkit = managedForm.getToolkit();
		//
		Section section = toolkit.createSection(parent, ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR);
		section.setText("Empty Master Section");
		//
		Composite composite = toolkit.createComposite(section, SWT.NONE);
		toolkit.paintBordersFor(composite);
		section.setClient(composite);
		GridLayout gl_composite = new GridLayout(3, false);
		gl_composite.horizontalSpacing = 15;
		composite.setLayout(gl_composite);

		linkAdd = new Link(composite, SWT.NONE);
		toolkit.adapt(linkAdd, true, true);
		linkAdd.setText("<a>neue Therapie</a>");
		linkAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (localPatientSelection == null)
					return;
				Therapy t = new Therapy();
				t.setTherapyStart(new Date());
				t.setTherapyEnd(new Date());
				t.setPatient(localPatientSelection);

				EntityTransaction trans = entityManager.getTransaction();
				trans.begin();
				entityManager.persist(t);
				trans.commit();

				trans.begin();
				entityManager.refresh(localPatientSelection);
				trans.commit();

				refresh();
			}
		});

		linkRemove = new Link(composite, SWT.NONE);
		toolkit.adapt(linkRemove, true, true);
		linkRemove.setText("<a>ausgew\u00E4hlte Therapie l\u00F6schen</a>");
		linkRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (localTherapySelection == null)
					return;

				EntityTransaction trans = entityManager.getTransaction();
				trans.begin();
				entityManager.remove(localTherapySelection);
				trans.commit();

				trans.begin();
				entityManager.refresh(localPatientSelection);

				trans.commit();
				refresh();
			}
		});

		new Label(composite, SWT.NONE);

		text = new Text(composite, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		toolkit.adapt(text, true, true);

		therapiesViewer = new ListViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
		List therapiesTree = therapiesViewer.getList();
		GridData gd_tableTherapies = new GridData(SWT.FILL, SWT.FILL, false, true, 3, 1);
		gd_tableTherapies.heightHint = 42;
		gd_tableTherapies.widthHint = 102;
		therapiesTree.setLayoutData(gd_tableTherapies);

		therapiesViewer.setContentProvider(new ArrayContentProvider());
		therapiesViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Therapy) element).toString();
			}
		});

		final SectionPart therapyPart = new SectionPart(section);
		managedForm.addPart(therapyPart);
		therapiesViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				managedForm.fireSelectionChanged(therapyPart, event.getSelection());
				IStructuredSelection structuredSelection = (IStructuredSelection) event.getSelection();
				Therapy therapy = (Therapy) structuredSelection.getFirstElement();
				if (localTherapySelection != null && !localTherapySelection.equals(therapy))
					entityManager.detach(localTherapySelection);
				localTherapySelection = entityManager.merge(therapy);
				provider.setSelection(Therapy.class, therapy);

			}
		});

		final SectionPart spart = new SectionPart(section);
		managedForm.addPart(spart);

		// selectionListener for Patient.class
		IGlobalSelectionListener<Patient> selectionListener = new IGlobalSelectionListener<Patient>() {

			@Override
			public void selectionChanged(Patient selection) {
				if (selection == null) {
					therapiesViewer.setInput(null);
				} else {
					localPatientSelection = entityManager.merge(selection);
					therapiesViewer.setInput(localPatientSelection.getTherapies());
				}
			}

			@Override
			public Class<Patient> getType() {
				return Patient.class;
			}
		};
		Activator.getBundleContext().registerService(IGlobalSelectionListener.class, selectionListener, null);

	}

	private void refresh() {
		if (localPatientSelection != null)
			therapiesViewer.setInput(localPatientSelection.getTherapies());
	}

	/**
	 * Register the pages.
	 * 
	 * @param part
	 */
	@Override
	protected void registerPages(DetailsPart part) {
		part.registerPage(Therapy.class, new TherapyDetailPage());
	}

	/**
	 * Create the toolbar actions.
	 * 
	 * @param managedForm
	 */
	@Override
	protected void createToolBarActions(IManagedForm managedForm) {
		// Create the toolbar actions
	}
}
