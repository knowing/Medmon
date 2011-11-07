package de.lmu.ifi.dbs.medmon.medic.ui.views;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import de.lmu.ifi.dbs.medmon.base.ui.adapter.IWorkbenchColumnAdapter;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Therapy;
import de.lmu.ifi.dbs.medmon.database.model.TherapyResult;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionListener;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.util.DetailBlockSelectionAdapter;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.jface.viewers.TreeViewer;

public class PatientFileDetailBlock extends MasterDetailsBlock {

	private FormToolkit toolkit;
	private Text text;
	private Calendar calendar = Calendar.getInstance();
	private Link linkAdd;
	private Link linkRemove;
	private IGlobalSelectionProvider selectionProvider;
	private DetailBlockSelectionAdapter selectionAdapter;
	private Tree tree;
	private TreeViewer treeViewerTherapies;

	/**
	 * Create the master details block.
	 */
	public PatientFileDetailBlock() {
		// Create the master details block
		selectionProvider = new GlobalSelectionProvider(Activator.getBundleContext());
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
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.horizontalSpacing = 15;
		composite.setLayout(gl_composite);

		linkAdd = new Link(composite, SWT.NONE);
		toolkit.adapt(linkAdd, true, true);
		linkAdd.setText("<a>neue Therapie</a>");
		linkAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Patient selectedPatient = selectionProvider.getSelection(Patient.class);
				if (selectedPatient == null)
					return;

				EntityManager entityManager = JPAUtil.createEntityManager();
				entityManager.getTransaction().begin();

				Therapy t = new Therapy();
				t.setTherapyStart(new Date());
				t.setTherapyEnd(new Date());
				t.setPatient(selectedPatient);
				selectedPatient.getTherapies().add(t);

				entityManager.persist(t);
				entityManager.merge(selectedPatient);

				entityManager.getTransaction().commit();
				entityManager.close();

				selectionProvider.updateSelection(Patient.class);
			}
		});

		linkRemove = new Link(composite, SWT.NONE);
		toolkit.adapt(linkRemove, true, true);
		linkRemove.setText("<a>ausgew\u00E4hlte Therapie l\u00F6schen</a>");
		linkRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Therapy selectedTherapy = selectionProvider.getSelection(Therapy.class);

				if (selectedTherapy == null) {
					return;
				}

				EntityManager entityManager = JPAUtil.createEntityManager();
				entityManager.getTransaction().begin();

				selectedTherapy = entityManager.merge(selectedTherapy);
				entityManager.remove(selectedTherapy);
				entityManager.getTransaction().commit();
				entityManager.close();

				selectionProvider.updateSelection(Patient.class);
				selectionProvider.setSelection(Therapy.class, null);
			}
		});

		text = new Text(composite, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		toolkit.adapt(text, true, true);

		final SectionPart sectionPart = new SectionPart(section);
		managedForm.addPart(sectionPart);

		selectionAdapter = new DetailBlockSelectionAdapter(managedForm, sectionPart, selectionProvider, new Class[] { Therapy.class,
				TherapyResult.class });

		treeViewerTherapies = new TreeViewer(composite, SWT.BORDER);
		tree = treeViewerTherapies.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		toolkit.paintBordersFor(tree);
		treeViewerTherapies.setContentProvider(new BaseWorkbenchContentProvider());
		treeViewerTherapies.setLabelProvider(new WorkbenchLabelProvider());
		treeViewerTherapies.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				selectionAdapter.setSelection(event.getSelection());
			}
		});

		final SectionPart therapyPart = new SectionPart(section);
		managedForm.addPart(therapyPart);

		final SectionPart spart = new SectionPart(section);
		managedForm.addPart(spart);

		selectionProvider.registerListener(new IGlobalSelectionListener<Patient>() {
			@Override
			public void selectionChanged(Patient selection) {
				if (selection == null) {
					treeViewerTherapies.setInput(null);
				} else {
					EntityManager entityManager = JPAUtil.createEntityManager();
					entityManager.getTransaction().begin();
					selection = entityManager.merge(selection);
					entityManager.getTransaction().commit();
					entityManager.close();

					treeViewerTherapies.setInput(selection);
					
				}
			}

			@Override
			public void selectionUpdated() {
				treeViewerTherapies.refresh();
			}
			
			@Override
			public Class<Patient> getType() {
				return Patient.class;
			}
		});
	}

	/**
	 * Register the pages.
	 * 
	 * @param part
	 */
	@Override
	protected void registerPages(DetailsPart part) {
		part.registerPage(Therapy.class, new TherapyDetailPage());
		part.registerPage(TherapyResult.class, new TherapyResultDetailPage());
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
