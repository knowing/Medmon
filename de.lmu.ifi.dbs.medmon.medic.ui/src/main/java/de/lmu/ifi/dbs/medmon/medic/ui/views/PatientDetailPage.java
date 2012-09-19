package de.lmu.ifi.dbs.medmon.medic.ui.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;

public class PatientDetailPage implements IDetailsPage {

	private IManagedForm	managedForm;
	private Text			textTherapy;
	private Text			textSuccess;
	private Text			txtNewText;
	private Scale			scaleSuccess;

	/**
	 * Create the details page.
	 */
	public PatientDetailPage() {
		// Create the details page
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
		Section section = toolkit.createSection(parent, ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR);
		section.setText("Empty Section");
		//
		Composite composite = toolkit.createComposite(section, SWT.NONE);
		// toolkit.paintBordersFor(composite);
		section.setClient(composite);
		composite.setLayout(new GridLayout(5, false));

		Label lblTherapy = new Label(composite, SWT.NONE);
		toolkit.adapt(lblTherapy, true, true);
		lblTherapy.setText("Ma\u00DFnahmen:");

		textTherapy = new Text(composite, SWT.BORDER);
		textTherapy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		toolkit.adapt(textTherapy, true, true);

		Label lblFrom = new Label(composite, SWT.NONE);
		GridData gd_lblFrom = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblFrom.widthHint = 100;
		lblFrom.setLayoutData(gd_lblFrom);
		toolkit.adapt(lblFrom, true, true);
		lblFrom.setText("Von:");

		CDateTime dateTime = new CDateTime(composite, CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM);
		GridData gd_dateTime = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_dateTime.heightHint = 20;
		dateTime.setLayoutData(gd_dateTime);

		toolkit.adapt(dateTime);
		toolkit.paintBordersFor(dateTime);

		Label lblNewLabel_2 = new Label(composite, SWT.NONE);
		GridData gd_lblNewLabel_2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel_2.widthHint = 100;
		lblNewLabel_2.setLayoutData(gd_lblNewLabel_2);
		toolkit.adapt(lblNewLabel_2, true, true);
		lblNewLabel_2.setText("Bis:");

		CDateTime dateTime_1 = new CDateTime(composite, CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM);
		GridData gd_dateTime_1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_dateTime_1.heightHint = 20;
		dateTime_1.setLayoutData(gd_dateTime_1);
		toolkit.adapt(dateTime_1);
		toolkit.paintBordersFor(dateTime_1);
		new Label(composite, SWT.NONE);

		Label lblNewLabel_3 = new Label(composite, SWT.NONE);
		toolkit.adapt(lblNewLabel_3, true, true);
		lblNewLabel_3.setText("Erfolg:");

		scaleSuccess = new Scale(composite, SWT.NONE);
		scaleSuccess.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				int value = scaleSuccess.getMaximum() - scaleSuccess.getSelection() + scaleSuccess.getMinimum();
				textSuccess.setText(value + "%");
			}
		});

		scaleSuccess.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		toolkit.adapt(scaleSuccess, true, true);

		textSuccess = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		GridData gd_textSuccess = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textSuccess.widthHint = 40;
		textSuccess.setLayoutData(gd_textSuccess);
		toolkit.adapt(textSuccess, true, true);
		new Label(composite, SWT.NONE);

		ImageHyperlink mghprlnkNewImagehyperlink = toolkit.createImageHyperlink(composite, SWT.NONE);
		mghprlnkNewImagehyperlink.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		toolkit.paintBordersFor(mghprlnkNewImagehyperlink);
		mghprlnkNewImagehyperlink.setText("New ImageHyperlink");
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		Group groupComment = new Group(composite, SWT.NONE);
		groupComment.setText("Kommentar:");
		groupComment.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 5, 1));
		toolkit.adapt(groupComment);
		// toolkit.paintBordersFor(groupComment);
		groupComment.setLayout(new FillLayout(SWT.HORIZONTAL));

		txtNewText = toolkit.createText(groupComment, "New Text", SWT.MULTI);

	}

	public void dispose() {
		// Dispose
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
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		update();
	}

	public void commit(boolean onSave) {
		// Commit
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
