package de.lmu.ifi.dbs.medmon.medic.ui.views;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.layout.GridData;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Group;

import de.lmu.ifi.dbs.medmon.base.ui.adapter.IWorkbenchColumnAdapter;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Therapy;
import de.lmu.ifi.dbs.medmon.database.model.TherapyResult;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionListener;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.RowLayout;
import swing2swt.layout.FlowLayout;
import org.eclipse.swt.widgets.Link;

public class TherapyResultDetailPage implements IDetailsPage {

	private IManagedForm managedForm;
	private Text textTherapy;
	private Text textSuccess;
	private Scale scaleSuccess;
	private Therapy therapy;
	private CDateTime dateStart;
	private Listener successChangedListener;
	private IGlobalSelectionProvider selectionProvider;

	/**
	 * Create the details page.
	 */
	public TherapyResultDetailPage() {
		// Create the details page
		selectionProvider = new GlobalSelectionProvider(Activator.getBundleContext());
	}

	/**
	 * Initialize the details page.
	 * 
	 * @param form
	 */
	public void initialize(IManagedForm form) {
		managedForm = form;
	}

	/**
	 * Create contents of the details page.
	 * 
	 * @param parent
	 */
	public void createContents(Composite parent) {
		FormToolkit toolkit = managedForm.getToolkit();
		parent.setLayout(new FillLayout());
		//
		Section sctnTherapie = toolkit.createSection(parent, ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR);
		sctnTherapie.setText("Therapie");
		//
		Composite composite = toolkit.createComposite(sctnTherapie, SWT.NONE);
		// toolkit.paintBordersFor(composite);
		sctnTherapie.setClient(composite);
		composite.setLayout(new GridLayout(4, false));

		Label lblTherapy = new Label(composite, SWT.NONE);
		toolkit.adapt(lblTherapy, true, true);
		lblTherapy.setText("Ma\u00DFnahmen:");

		textTherapy = new Text(composite, SWT.BORDER);
		textTherapy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		toolkit.adapt(textTherapy, true, true);

		Label lblTimestamp = new Label(composite, SWT.NONE);
		GridData gd_lblTimestamp = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblTimestamp.widthHint = 80;
		lblTimestamp.setLayoutData(gd_lblTimestamp);
		toolkit.adapt(lblTimestamp, true, true);
		lblTimestamp.setText("Timestamp:");

		dateStart = new CDateTime(composite, CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM);
		GridData gd_dateStart = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_dateStart.widthHint = 100;
		gd_dateStart.heightHint = 20;
		dateStart.setLayoutData(gd_dateStart);

		toolkit.adapt(dateStart);
		toolkit.paintBordersFor(dateStart);

		Label lblPlaceholder2 = new Label(composite, SWT.NONE);
		lblPlaceholder2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		toolkit.adapt(lblPlaceholder2, true, true);
		new Label(composite, SWT.NONE);

		Label lblNewLabel_3 = new Label(composite, SWT.NONE);
		toolkit.adapt(lblNewLabel_3, true, true);
		lblNewLabel_3.setText("Erfolg:");

		successChangedListener = new Listener() {
			private int success;

			public void handleEvent(Event event) {
				success = scaleSuccess.getSelection();
				textSuccess.setText(success + "%");
			}
		};

		scaleSuccess = new Scale(composite, SWT.NONE);
		scaleSuccess.addListener(SWT.Selection, successChangedListener);
		scaleSuccess.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		toolkit.adapt(scaleSuccess, true, true);

		textSuccess = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		textSuccess.setText("0%");
		GridData gd_textSuccess = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textSuccess.widthHint = 40;
		textSuccess.setLayoutData(gd_textSuccess);
		toolkit.adapt(textSuccess, true, true);

		Group groupComment = new Group(composite, SWT.NONE);
		groupComment.setText("Kommentar:");
		groupComment.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 4, 1));
		toolkit.adapt(groupComment);
		toolkit.paintBordersFor(groupComment);
		groupComment.setLayout(new FillLayout(SWT.HORIZONTAL));

		Text txtComment = toolkit.createText(groupComment, "New Text", SWT.MULTI);

		Composite compositeLinks = new Composite(composite, SWT.NONE);
		compositeLinks.setLayout(new GridLayout(2, false));
		compositeLinks.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1));
		toolkit.adapt(compositeLinks);
		toolkit.paintBordersFor(compositeLinks);

		Label lblPlaceholder = new Label(compositeLinks, SWT.NONE);
		lblPlaceholder.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		toolkit.adapt(lblPlaceholder, true, true);

		Link linkSave = new Link(compositeLinks, SWT.NONE);
		toolkit.adapt(linkSave, true, true);
		linkSave.setText("<a>Speichern</a>");
		linkSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				commit(true);
			}
		});
	}

	public void dispose() {
		// Dispose
		selectionProvider.unregister();
	}

	public void setFocus() {
		// Set focus
	}

	private void update() {
		// Update
	}

	public boolean setFormInput(Object input) {
		return false;
	}

	public void selectionChanged(IFormPart part, ISelection selection) {
		update();
	}

	public void commit(boolean onSave) {
	}

	public boolean isDirty() {
		return false;
	}

	public boolean isStale() {
		return false;
	}

	public void refresh() {
		update();
	}

}
