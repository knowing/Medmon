package de.lmu.ifi.dbs.medmon.medic.ui.views;

import java.util.Date;

import javax.persistence.EntityManager;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.database.entity.Therapy;
import de.lmu.ifi.dbs.medmon.database.entity.TherapyResult;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.services.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.services.IGlobalSelectionListener;
import de.lmu.ifi.dbs.medmon.services.IGlobalSelectionProvider;

/**
 * 
 * @author Stephan Picker, Nepomuk Seiler
 * @version 0.2
 * @since 15.03.2012
 */
public class PatientFileDetailBlock extends MasterDetailsBlock {
	public PatientFileDetailBlock() {
	}

	private static Logger				log	= LoggerFactory.getLogger(PatientFileDetailBlock.class);

	private FormToolkit					toolkit;
	private IGlobalSelectionProvider	selectionProvider;
	private TreeViewer					therapiesViewer;
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
		therapiesViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		toolkit.paintBordersFor(therapiesViewer.getTree());
		therapiesViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				Date d1, d2;
				if (e1 instanceof Therapy)
					d1 = ((Therapy) e1).getTherapyStart();
				else if (e1 instanceof TherapyResult)
					d1 = ((TherapyResult) e1).getTimestamp();
				else
					d1 = new Date();
				
				if (e2 instanceof Therapy)
					d2 = ((Therapy) e2).getTherapyStart();
				else if (e2 instanceof TherapyResult)
					d2 = ((TherapyResult) e2).getTimestamp();
				else
					d2 = new Date();
				if(d1 == null)
					return -1;
				return d1.compareTo(d2);
			}
		});
		therapiesViewer.setContentProvider(new BaseWorkbenchContentProvider());
		therapiesViewer.setLabelProvider(new WorkbenchLabelProvider());
		therapiesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				managedForm.fireSelectionChanged(sectionPart, event.getSelection());
				if(event.getSelection().isEmpty())
					return;
				IStructuredSelection structedSelection = (IStructuredSelection) event.getSelection();
				Object elem = structedSelection.getFirstElement();
				therapiesViewer.expandToLevel(elem,2);
			}
		});

		Composite cLinkPanel = new Composite(composite, SWT.NONE);
		cLinkPanel.setLayout(new GridLayout(2, false));
		cLinkPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		toolkit.adapt(cLinkPanel);
		toolkit.paintBordersFor(cLinkPanel);

		Link linkAdd = new Link(cLinkPanel, SWT.NONE);
		toolkit.adapt(linkAdd, true, true);
		linkAdd.setText("<a>neue Therapie</a>");
		linkAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (localPatientSelection == null)
					return;

				// Activator.getDBModelService().createTherapy(localPatientSelection);
				EntityManager tempEM = Activator.getEntityManagerService().createEntityManager();
				tempEM.getTransaction().begin();
				Patient mPatient = tempEM.find(Patient.class, localPatientSelection.getId());
				Therapy therapy = new Therapy("<Neue Therapie>", mPatient);
				tempEM.persist(therapy);
				tempEM.getTransaction().commit();
				tempEM.close();

				selectionProvider.setSelection(Therapy.class, therapy);
				therapiesViewer.setSelection(new StructuredSelection(therapy), true);
				selectionProvider.updateSelection(Patient.class);
			}
		});

		Label lblPlaceholder1 = new Label(cLinkPanel, SWT.NONE);
		lblPlaceholder1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		toolkit.adapt(lblPlaceholder1, true, true);

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
					return;
				}
				localPatientSelection = selection;

				/************************************************************
				 * Database Access Begin
				 ************************************************************/

				EntityManager tempEM = Activator.getEntityManagerService().createEntityManager();
				Patient mPatient = tempEM.find(Patient.class, selection.getId());
				Object[] expandedElements = therapiesViewer.getExpandedElements();
				therapiesViewer.setInput(mPatient);
				therapiesViewer.setExpandedElements(expandedElements);
				tempEM.close();

				/************************************************************
				 * Database Access End
				 ************************************************************/
			}

			@Override
			public void selectionUpdated() {

				/************************************************************
				 * Database Access Begin
				 ************************************************************/
				EntityManager tempEM = Activator.getEntityManagerService().createEntityManager();
				Patient mPatient = tempEM.find(Patient.class, localPatientSelection.getId());
				tempEM.refresh(mPatient);
				Object[] expandedElements = therapiesViewer.getExpandedElements();
				log.debug("Selection updated with " + mPatient);
				therapiesViewer.setInput(mPatient);
				therapiesViewer.setExpandedElements(expandedElements);
				tempEM.close();

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
