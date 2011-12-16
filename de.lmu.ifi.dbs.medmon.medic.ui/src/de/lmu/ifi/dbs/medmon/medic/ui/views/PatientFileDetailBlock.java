package de.lmu.ifi.dbs.medmon.medic.ui.views;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Therapy;
import de.lmu.ifi.dbs.medmon.database.model.TherapyResult;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionListener;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;

public class PatientFileDetailBlock extends MasterDetailsBlock {
	public PatientFileDetailBlock() {
	}

	private FormToolkit					toolkit;
	private IGlobalSelectionProvider	selectionProvider;
	private TreeViewer					therapiesViewer;
	private EntityManager				entityManager;
	private Patient						localPatientSelection;

	/**
	 * Create contents of the master details block.
	 * 
	 * @param managedForm
	 * @param parent
	 */
	@Override
	protected void createMasterPart(final IManagedForm managedForm, Composite parent) {
		toolkit = managedForm.getToolkit();
		entityManager = JPAUtil.createEntityManager();
		selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());

		Section section = toolkit.createSection(parent, ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR);
		section.setText("Patientenakte");
	
		Composite composite = toolkit.createComposite(section, SWT.NONE);
		toolkit.paintBordersFor(composite);
		section.setClient(composite);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.horizontalSpacing = 15;
		composite.setLayout(gl_composite);

		Text text = new Text(composite, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		toolkit.adapt(text, true, true);

		final SectionPart sectionPart = new SectionPart(section);
		managedForm.addPart(sectionPart);

		therapiesViewer = new TreeViewer(composite, SWT.BORDER);
		Tree tree = therapiesViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		toolkit.paintBordersFor(tree);

		Composite composite_1 = new Composite(composite, SWT.NONE);
		composite_1.setLayout(new GridLayout(2, false));
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		toolkit.adapt(composite_1);
		toolkit.paintBordersFor(composite_1);

		Link linkAdd = new Link(composite_1, SWT.NONE);
		toolkit.adapt(linkAdd, true, true);
		linkAdd.setText("<a>neue Therapie</a>");
		linkAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (localPatientSelection == null)
					return;

				/************************************************************
				 * Database Access Begin
				 ************************************************************/

				Therapy mTherapy = new Therapy();

				mTherapy.setCaption("neue Therapie");
				mTherapy.setComment("kein Kommentar.");
				mTherapy.setTherapyStart(new Date());
				mTherapy.setTherapyEnd(new Date());
				mTherapy.setPatient(localPatientSelection);

				entityManager.getTransaction().begin();
				entityManager.persist(mTherapy);
				entityManager.getTransaction().commit();

				/************************************************************
				 * Database Access End
				 ************************************************************/

				selectionProvider.updateSelection(Patient.class);
			}
		});

		Label lblPlaceholder1 = new Label(composite_1, SWT.NONE);
		lblPlaceholder1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		toolkit.adapt(lblPlaceholder1, true, true);
		linkAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

			}
		});
		therapiesViewer.setContentProvider(new BaseWorkbenchContentProvider());
		therapiesViewer.setLabelProvider(new WorkbenchLabelProvider());
		therapiesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				managedForm.fireSelectionChanged(sectionPart, event.getSelection());
			}
		});

		final SectionPart therapyPart = new SectionPart(section);
		managedForm.addPart(therapyPart);

		final SectionPart spart = new SectionPart(section);
		managedForm.addPart(spart);

		selectionProvider.registerSelectionListener(new IGlobalSelectionListener<Patient>() {
			@Override
			public void selectionChanged(Patient selection) {
				if (selection == null) {
					localPatientSelection = null;
					therapiesViewer.setInput(null);
				} else {

					/************************************************************
					 * Database Access Begin
					 ************************************************************/

					entityManager.getTransaction().begin();
					localPatientSelection = entityManager.merge(selection);
					entityManager.getTransaction().commit();

					therapiesViewer.setInput(localPatientSelection);

					/************************************************************
					 * Database Access End
					 ************************************************************/
				}
			}

			@Override
			public void selectionUpdated() {

				/************************************************************
				 * Database Access Begin
				 ************************************************************/

				entityManager.getTransaction().begin();
				entityManager.refresh(localPatientSelection);
				entityManager.getTransaction().commit();

				therapiesViewer.refresh();

				/************************************************************
				 * Database Access End
				 ************************************************************/
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
