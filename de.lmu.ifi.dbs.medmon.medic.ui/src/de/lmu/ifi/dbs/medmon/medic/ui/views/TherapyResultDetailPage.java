package de.lmu.ifi.dbs.medmon.medic.ui.views;

import java.io.IOException;
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
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Group;

import de.lmu.ifi.dbs.medmon.base.ui.adapter.IWorkbenchColumnAdapter;
import de.lmu.ifi.dbs.medmon.database.model.Data;
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
import org.eclipse.swt.widgets.Link;

public class TherapyResultDetailPage implements IDetailsPage {

	private IManagedForm				managedForm;
	private Text						textTherapy;
	private Text						textSuccess;
	private Scale						scaleSuccess;
	private CDateTime					dateTimestamp;
	private Listener					successChangedListener;
	private IGlobalSelectionProvider	selectionProvider;
	private EntityManager				entityManager;
	private Text						textComment;
	private TherapyResult				localTherapyResultSelection;
	private boolean						isDirty		= false;
	private boolean						ignoreDirty	= false;

	/**
	 * Create the details page.
	 */
	public TherapyResultDetailPage() {
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
		entityManager = JPAUtil.createEntityManager();
		selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
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

		SelectionAdapter dirtyListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isDirty = true;
			}
		};

		Label lblTherapy = new Label(composite, SWT.NONE);
		toolkit.adapt(lblTherapy, true, true);
		lblTherapy.setText("Ma\u00DFnahmen:");

		textTherapy = new Text(composite, SWT.BORDER);
		textTherapy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		toolkit.adapt(textTherapy, true, true);
		textTherapy.addModifyListener(new DirtyListener());

		Label lblTimestamp = new Label(composite, SWT.NONE);
		GridData gd_lblTimestamp = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblTimestamp.widthHint = 80;
		lblTimestamp.setLayoutData(gd_lblTimestamp);
		toolkit.adapt(lblTimestamp, true, true);
		lblTimestamp.setText("Timestamp:");

		dateTimestamp = new CDateTime(composite, CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM);
		GridData gd_dateTimestamp = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_dateTimestamp.widthHint = 100;
		gd_dateTimestamp.heightHint = 20;
		dateTimestamp.setLayoutData(gd_dateTimestamp);
		dateTimestamp.addSelectionListener(new DirtyListener());

		toolkit.adapt(dateTimestamp);
		toolkit.paintBordersFor(dateTimestamp);

		Label lblPlaceholder2 = new Label(composite, SWT.NONE);
		lblPlaceholder2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		toolkit.adapt(lblPlaceholder2, true, true);
		new Label(composite, SWT.NONE);

		Label lblNewLabel_3 = new Label(composite, SWT.NONE);
		toolkit.adapt(lblNewLabel_3, true, true);
		lblNewLabel_3.setText("Erfolg:");

		successChangedListener = new Listener() {
			private int	success;

			public void handleEvent(Event event) {
				success = scaleSuccess.getSelection();
				textSuccess.setText(success + "%");
			}
		};

		scaleSuccess = new Scale(composite, SWT.NONE);
		scaleSuccess.addListener(SWT.Selection, successChangedListener);
		scaleSuccess.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		toolkit.adapt(scaleSuccess, true, true);
		scaleSuccess.addSelectionListener(new DirtyListener());

		textSuccess = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		textSuccess.setText("0%");
		GridData gd_textSuccess = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textSuccess.widthHint = 40;
		textSuccess.setLayoutData(gd_textSuccess);
		toolkit.adapt(textSuccess, true, true);
		textSuccess.addSelectionListener(new DirtyListener());

		Group groupComment = new Group(composite, SWT.NONE);
		groupComment.setText("Kommentar:");
		groupComment.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 4, 1));
		toolkit.adapt(groupComment);
		toolkit.paintBordersFor(groupComment);
		groupComment.setLayout(new FillLayout(SWT.HORIZONTAL));

		textComment = toolkit.createText(groupComment, "New Text", SWT.MULTI);

		Composite compositeLinks = new Composite(composite, SWT.NONE);
		compositeLinks.setLayout(new GridLayout(3, false));
		compositeLinks.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1));
		toolkit.adapt(compositeLinks);
		toolkit.paintBordersFor(compositeLinks);

		Link linkDelete = new Link(compositeLinks, SWT.NONE);
		toolkit.adapt(linkDelete, true, true);
		linkDelete.setText("<a>l\u00F6schen</a>");
		linkDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				System.out.println("Xxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
				commit(true);
				TherapyResult selectedTherapyResult = selectionProvider.getSelection(TherapyResult.class);

				/************************************************************
				 * Database Access Begin
				 ************************************************************/

				entityManager.getTransaction().begin();
				// TherapyResult mTherapyResult =
				// entityManager.merge(selectedTherapyResult);
				// Data mData = entityManager.merge(mTherapyResult.getData());
				// entityManager.merge(selectedTherapyResult.getTherapy());

				entityManager.remove(selectedTherapyResult);

				entityManager.getTransaction().commit();
				entityManager.clear();

				// try {
				// Activator.getPatientService().remove(mData);
				// } catch (IOException e1) {
				// e1.printStackTrace();
				// }

				/************************************************************
				 * Database Access End
				 ************************************************************/

				selectionProvider.setSelection(TherapyResult.class, null);
				selectionProvider.updateSelection(Therapy.class);
				selectionProvider.updateSelection(Patient.class);
			}
		});

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
		entityManager.close();
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

		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		localTherapyResultSelection = (TherapyResult) structuredSelection.getFirstElement();

		selectionProvider.setSelection(TherapyResult.class, localTherapyResultSelection);

		entityManager.getTransaction().begin();
		TherapyResult mTherapyResult = entityManager.find(TherapyResult.class, localTherapyResultSelection.getId());
		textTherapy.setText(mTherapyResult.getCaption());
		textComment.setText(mTherapyResult.getComment());
		dateTimestamp.setSelection(mTherapyResult.getTimestamp());
		scaleSuccess.setSelection(mTherapyResult.getSuccess());
		entityManager.getTransaction().commit();
		entityManager.clear();

		successChangedListener.handleEvent(null);
		update();
	}

	public void commit(boolean onSave) {

		if (isDirty) {
			System.out.println("save");
			ignoreDirty = true;
			TherapyResult mTherapyResult = entityManager.find(TherapyResult.class, localTherapyResultSelection.getId());
			
			entityManager.getTransaction().begin();
			mTherapyResult.setCaption(textTherapy.getText());
			mTherapyResult.setComment(textTherapy.getText());
			mTherapyResult.setSuccess(scaleSuccess.getSelection());
			mTherapyResult.setTimestamp(dateTimestamp.getSelection());
			entityManager.merge(mTherapyResult);
			entityManager.getTransaction().commit();
			entityManager.clear();

			selectionProvider.updateSelection(TherapyResult.class);
			selectionProvider.updateSelection(Patient.class);
			ignoreDirty = false;
			isDirty = false;
		}
	}

	public boolean isDirty() {
		return true;
	}

	public boolean isStale() {
		return false;
	}

	public void refresh() {
		update();
	}

	private class DirtyListener extends SelectionAdapter implements ModifyListener {

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (!ignoreDirty)
				isDirty = true;
		}

		@Override
		public void modifyText(ModifyEvent e) {
			if (!ignoreDirty)
				isDirty = true;
		}

	}

}
