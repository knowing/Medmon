package de.lmu.ifi.dbs.medmon.medic.ui.views;

import java.util.Calendar;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
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
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.selectionadapters.SACreateTherapy;
import de.lmu.ifi.dbs.medmon.medic.ui.selectionadapters.SADeleteTherapy;
import org.eclipse.swt.widgets.Label;

public class PatientFileDetailBlock extends MasterDetailsBlock {

	private FormToolkit toolkit;
	private Text text;
	private Calendar calendar = Calendar.getInstance();
	private Link linkAdd;
	private IGlobalSelectionProvider selectionProvider;
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
		linkAdd.addSelectionListener(new SACreateTherapy());
		new Label(composite, SWT.NONE);

		text = new Text(composite, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		toolkit.adapt(text, true, true);

		final SectionPart sectionPart = new SectionPart(section);
		managedForm.addPart(sectionPart);

		treeViewerTherapies = new TreeViewer(composite, SWT.BORDER);
		tree = treeViewerTherapies.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		toolkit.paintBordersFor(tree);
		treeViewerTherapies.setContentProvider(new BaseWorkbenchContentProvider());
		treeViewerTherapies.setLabelProvider(new WorkbenchLabelProvider());
		treeViewerTherapies.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				managedForm.fireSelectionChanged(sectionPart, event.getSelection());
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
