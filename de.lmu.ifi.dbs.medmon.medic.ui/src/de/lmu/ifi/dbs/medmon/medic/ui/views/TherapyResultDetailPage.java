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
	private EntityManager				workerEM;
	private Text						textComment;
	private TherapyResult				localTherapyResultSelection;
	private boolean						isDirty				= false;
	private boolean						ignoreModification	= false;
	private DirtyListener				dirtyListener		= new DirtyListener();

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
		workerEM = JPAUtil.createEntityManager();
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

		Label lblTherapy = new Label(composite, SWT.NONE);
		toolkit.adapt(lblTherapy, true, true);
		lblTherapy.setText("Ma\u00DFnahmen:");

		textTherapy = new Text(composite, SWT.BORDER);
		textTherapy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		toolkit.adapt(textTherapy, true, true);
		textTherapy.addModifyListener(dirtyListener);

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
		dateTimestamp.addSelectionListener(dirtyListener);

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
		scaleSuccess.addSelectionListener(dirtyListener);

		textSuccess = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		textSuccess.setText("0%");
		GridData gd_textSuccess = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textSuccess.widthHint = 40;
		textSuccess.setLayoutData(gd_textSuccess);
		toolkit.adapt(textSuccess, true, true);
		textSuccess.addSelectionListener(dirtyListener);

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

				//needed, so this page can be discarded with all progress saved.
				commit(true);
				
				try {
					Activator.getPatientService().deleteTherapyResult(localTherapyResultSelection);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				
//				/************************************************************
//				 * Database Access Begin
//				 ************************************************************/
//
//				workerEM.getTransaction().begin();
//				TherapyResult mTherapyResult = workerEM.find(TherapyResult.class, selectedTherapyResult.getId());
//
//				if (mTherapyResult.getData() != null) {
//					try {
//						Activator.getPatientService().deleteData(mTherapyResult.getData());
//					} catch (IOException e1) {
//						e1.printStackTrace();
//					}
//				}
//
//				workerEM.remove(mTherapyResult);
//				workerEM.getTransaction().commit();
//				workerEM.clear();
//
//				/************************************************************
//				 * Database Access End
//				 ************************************************************/
//
//				selectionProvider.setSelection(TherapyResult.class, null);
//				selectionProvider.updateSelection(Therapy.class);
//				selectionProvider.updateSelection(Patient.class);
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
		workerEM.close();
		selectionProvider.unregister();
	}

	public void setFocus() {
		// Set focus
	}

	private void update() {
		ignoreModification = true;
		successChangedListener.handleEvent(null);
		ignoreModification = false;
	}

	public boolean setFormInput(Object input) {
		return false;
	}

	public void selectionChanged(IFormPart part, ISelection selection) {

		/************************************************************
		 * Database Access Begin
		 ************************************************************/
		
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		localTherapyResultSelection = (TherapyResult) structuredSelection.getFirstElement();
		selectionProvider.setSelection(TherapyResult.class, localTherapyResultSelection);

		workerEM.getTransaction().begin();
		TherapyResult mTherapyResult = workerEM.find(TherapyResult.class, localTherapyResultSelection.getId());
		ignoreModification = true;
		textTherapy.setText(mTherapyResult.getCaption());
		textComment.setText(mTherapyResult.getComment());
		dateTimestamp.setSelection(mTherapyResult.getTimestamp());
		scaleSuccess.setSelection(mTherapyResult.getSuccess());
		workerEM.getTransaction().commit();
		ignoreModification = false;

		workerEM.clear();
		update();
		
		/************************************************************
		 * Database Access End
		 ************************************************************/
	}

	public void commit(boolean onSave) {

		/************************************************************
		 * Database Access Begin
		 ************************************************************/
		
		if (isDirty) {
			TherapyResult mTherapyResult = workerEM.find(TherapyResult.class, localTherapyResultSelection.getId());
			workerEM.getTransaction().begin();

			ignoreModification = true;
			mTherapyResult.setCaption(textTherapy.getText());
			mTherapyResult.setComment(textTherapy.getText());
			mTherapyResult.setSuccess(scaleSuccess.getSelection());
			mTherapyResult.setTimestamp(dateTimestamp.getSelection());
			ignoreModification = false;

			workerEM.merge(mTherapyResult);
			workerEM.getTransaction().commit();
			workerEM.clear();

			selectionProvider.updateSelection(TherapyResult.class);
			selectionProvider.updateSelection(Patient.class);
			isDirty = false;
		}
		
		/************************************************************
		 * Database Access End
		 ************************************************************/
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
			if (!ignoreModification)
				isDirty = true;
		}

		@Override
		public void modifyText(ModifyEvent e) {
			if (!ignoreModification)
				isDirty = true;
		}

	}

}
