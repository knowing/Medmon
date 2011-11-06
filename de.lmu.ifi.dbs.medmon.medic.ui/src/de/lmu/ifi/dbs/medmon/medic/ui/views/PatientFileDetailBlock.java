package de.lmu.ifi.dbs.medmon.medic.ui.views;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

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
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionListener;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.widgets.Link;

public class PatientFileDetailBlock extends MasterDetailsBlock {

	private FormToolkit toolkit;
	private Text text;
	private Table tableTherapies;
	private TableViewer tableViewerTherapies;
	private TableColumn clmTherapy;
	private TableViewerColumn cvTherapy;
	private TableColumn clmEnd;
	private TableViewerColumn cvEnd;
	private TableColumn clmStart;
	private TableViewerColumn cvStart;
	private Calendar calendar = Calendar.getInstance();
	private Link linkAdd;
	private Link linkRemove;

	/**
	 * Create the master details block.
	 */
	public PatientFileDetailBlock() {
		// Create the master details block
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

				GlobalSelectionProvider sp = new GlobalSelectionProvider();
				Patient selectedPatient = sp.getSelection(Patient.class);
				sp.unregister();

				if (selectedPatient == null)
					return;
				Therapy t = new Therapy();
				t.setTherapyStart(new Date());
				t.setTherapyEnd(new Date());
				t.setPatient(selectedPatient);
				selectedPatient.getTherapies().add(t);

				EntityManager entityManager = JPAUtil.createEntityManager();
				EntityTransaction trans = entityManager.getTransaction();
				trans.begin();

				entityManager.persist(t);
				entityManager.merge(selectedPatient);

				trans.commit();
				entityManager.close();

				tableViewerTherapies.refresh();

			}
		});

		linkRemove = new Link(composite, SWT.NONE);
		toolkit.adapt(linkRemove, true, true);
		linkRemove.setText("<a>ausgew\u00E4hlte Therapie l\u00F6schen</a>");
		linkRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GlobalSelectionProvider sp = new GlobalSelectionProvider();
				Therapy selectedTherapy = sp.getSelection(Therapy.class);

				if (selectedTherapy == null) {
					return;
				}

				EntityManager entityManager = JPAUtil.createEntityManager();
				EntityTransaction entityTransaction = entityManager.getTransaction();
				entityTransaction.begin();

				selectedTherapy = entityManager.merge(selectedTherapy);
				entityManager.remove(selectedTherapy);
				entityTransaction.commit();
				entityManager.close();

				sp.setSelection(Therapy.class, null);
				sp.unregister();

			}
		});

		new Label(composite, SWT.NONE);

		text = new Text(composite, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		toolkit.adapt(text, true, true);

		tableViewerTherapies = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
		tableTherapies = tableViewerTherapies.getTable();
		tableTherapies.setLinesVisible(true);
		tableTherapies.setHeaderVisible(true);
		GridData gd_tableTherapies = new GridData(SWT.FILL, SWT.FILL, false, true, 3, 1);
		gd_tableTherapies.heightHint = 42;
		gd_tableTherapies.widthHint = 102;
		tableTherapies.setLayoutData(gd_tableTherapies);
		toolkit.paintBordersFor(tableTherapies);
		tableViewerTherapies.setContentProvider(ArrayContentProvider.getInstance());

		final SectionPart therapyPart = new SectionPart(section);
		managedForm.addPart(therapyPart);
		tableViewerTherapies.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				managedForm.fireSelectionChanged(therapyPart, event.getSelection());
			}
		});

		cvTherapy = new TableViewerColumn(tableViewerTherapies, SWT.NONE);
		clmTherapy = cvTherapy.getColumn();
		clmTherapy.setWidth(100);
		clmTherapy.setText("Ma\u00DFnahmen");

		cvStart = new TableViewerColumn(tableViewerTherapies, SWT.NONE);
		clmStart = cvStart.getColumn();
		clmStart.setWidth(100);
		clmStart.setText("Start");

		cvEnd = new TableViewerColumn(tableViewerTherapies, SWT.NONE);
		clmEnd = cvEnd.getColumn();
		clmEnd.setWidth(100);
		clmEnd.setText("Ende");

		cvTherapy.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				Therapy t = (Therapy) cell.getElement();
				cell.setText("<>");
			}
		});
		cvStart.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				Therapy t = (Therapy) cell.getElement();
				calendar.setTime(t.getTherapyStart());
				String text = calendar.get(Calendar.DAY_OF_MONTH) + " "
						+ calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.GERMAN) + " " + calendar.get(Calendar.YEAR);
				cell.setText(text);
			}
		});
		cvEnd.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				Therapy t = (Therapy) cell.getElement();
				calendar.setTime(t.getTherapyEnd());
				String text = calendar.get(Calendar.DAY_OF_MONTH) + " "
						+ calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.GERMAN) + " " + calendar.get(Calendar.YEAR);
				cell.setText(text);
			}
		});

		final SectionPart spart = new SectionPart(section);
		managedForm.addPart(spart);

		// selectionListener for Patient.class
		IGlobalSelectionListener<Patient> selectionListener = new IGlobalSelectionListener<Patient>() {
			@Override
			public void selectionChanged(Patient selection) {
				if (selection == null) {
					tableViewerTherapies.setInput(null);
				} else {
					//EntityManager entityManager = JPAUtil.createEntityManager();
					//EntityTransaction entityTransaction = entityManager.getTransaction();
					//entityTransaction.begin();
					//selection = entityManager.merge(selection);
					//entityTransaction.commit();
					//entityManager.close();
					tableViewerTherapies.setInput(selection.getTherapies());
					}
			}

			@Override
			public Class<Patient> getType() {
				return Patient.class;
			}
		};
		Activator.getBundleContext().registerService(IGlobalSelectionListener.class, selectionListener, null);

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
