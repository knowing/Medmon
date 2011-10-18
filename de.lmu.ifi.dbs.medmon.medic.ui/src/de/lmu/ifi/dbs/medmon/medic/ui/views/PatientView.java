package de.lmu.ifi.dbs.medmon.medic.ui.views;

import static de.lmu.ifi.dbs.medmon.medic.ui.Activator.getImageDescriptor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.persistence.EntityManager;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.part.ViewPart;

import de.lmu.ifi.dbs.medmon.base.ui.viewer.DataViewer;
import de.lmu.ifi.dbs.medmon.database.model.Archiv;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.provider.ArchivLabelProvider;
import de.lmu.ifi.dbs.medmon.medic.ui.provider.ISharedImages;

public class PatientView extends ViewPart implements PropertyChangeListener {

	public static final String ID = "de.lmu.ifi.dbs.medmon.medic.ui.PatientView"; //$NON-NLS-1$
	private static final String PATIENT_TOOLBAR_CONTRIBUTIONS = "toolbar:de.lmu.ifi.dbs.medmon.medic.ui.PatientView.Patient";
	private static final String ARCHIV_TOOLBAR_CONTRIBUTIONS = "toolbar:de.lmu.ifi.dbs.medmon.medic.ui.PatientView.Archiv";
	private static final String DATA_TOOLBAR_CONTRIBUTIONS = "toolbar:de.lmu.ifi.dbs.medmon.medic.ui.PatientView.Data";

	private TabFolder tabFolder;

	/* Personal Data */
	private Text tLastname, tFirstname, tGender, tComment;
	private CDateTime dBirth;

	/* Archiv Tab */
	private Text tArchivSearch;
	private Table archivTable;
	private TableViewer archivViewer;
	private TabItem tabCluster;

	private FormToolkit toolkit;
	private TableViewer dataTableViewer;

	public PatientView() {
		IPatientService patientService = Activator.getPatientService();
		if (patientService != null)
			patientService.addPropertyChangeListener(IPatientService.PATIENT, this);
		else
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Fail", "Patient Service offline");
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		Composite container = toolkit.createComposite(parent);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));

		tabFolder = new TabFolder(container, SWT.BOTTOM);
		toolkit.adapt(tabFolder, true, true);

		createPersonalTab();
		createArchivTab();
		createDataTab();

		update();
	}

	/* ==================================== */
	/* =========== Personal Tab =========== */
	/* ==================================== */

	/**
	 * 
	 */
	private void createPersonalTab() {
		TabItem tPersonalData = new TabItem(tabFolder, SWT.NONE);
		tPersonalData.setText("Persoenliche Daten");
		tPersonalData.setImage(getImageDescriptor(ISharedImages.IMG_PATIENTS_16).createImage());

		Composite cPatient = new Composite(tabFolder, SWT.NONE);
		tPersonalData.setControl(cPatient);
		GridLayout cPatientLayout = new GridLayout(2, false);
		cPatientLayout.horizontalSpacing = 10;
		cPatient.setLayout(cPatientLayout);

/*		Label lName = new Label(cPatient, SWT.NONE);
		lName.setText("Name");
		lName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		tLastname = new Text(cPatient, SWT.BORDER | SWT.READ_ONLY);
		GridData data = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		data.widthHint = 150;
		tLastname.setLayoutData(data);

		Label lFirstname = new Label(cPatient, SWT.NONE);
		lFirstname.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lFirstname.setText("Vorname");

		tFirstname = new Text(cPatient, SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		data.widthHint = 150;
		tFirstname.setLayoutData(data);

		Label lBirth = new Label(cPatient, SWT.NONE);
		lBirth.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lBirth.setText("Geb.");

		dBirth = new CDateTime(cPatient, CDT.BORDER | CDT.DATE_SHORT);
		data = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		data.widthHint = 160;
		dBirth.setLayoutData(data);

		Label lGender = new Label(cPatient, SWT.NONE);
		lGender.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lGender.setText("Geschlecht");

		tGender = new Text(cPatient, SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		data.widthHint = 150;
		tGender.setLayoutData(data);

		Label label = new Label(cPatient, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("Vergleichsdaten");

		Composite cPatientCluster = new Composite(cPatient, SWT.NONE);
		GridLayout gl_cPatientCluster = new GridLayout(2, false);
		gl_cPatientCluster.marginWidth = 0;
		cPatientCluster.setLayout(gl_cPatientCluster);
		cPatientCluster.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

		tPatientCluster = new Text(cPatientCluster, SWT.BORDER);
		tPatientCluster.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tPatientCluster.setBounds(0, 0, 75, 25);

		Button bPatientLoadCluster = new Button(cPatientCluster, SWT.NONE);
		data = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		data.widthHint = 81;
		bPatientLoadCluster.setLayoutData(data);
		bPatientLoadCluster.setText("laden");
		bPatientLoadCluster.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tabFolder.setSelection(tabCluster);
			}
		});

		Label lComment = new Label(cPatient, SWT.NONE);
		lComment.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		lComment.setText("Kommentar");

		tComment = new Text(cPatient, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		tComment.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		*/
	}

	/* ==================================== */
	/* ============= Archiv Tab =========== */
	/* ==================================== */

	/**
	 * 
	 */
	private void createArchivTab() {

		TabItem tArchiv = new TabItem(tabFolder, SWT.NONE);
		tArchiv.setText("Patienten Akte");
		tArchiv.setImage(getImageDescriptor(ISharedImages.IMG_COMMENT_16).createImage());

		Composite cArchiv = new Composite(tabFolder, SWT.NONE);
		tArchiv.setControl(cArchiv);
		cArchiv.setLayout(new GridLayout(3, false));

		tArchivSearch = new Text(cArchiv, SWT.BORDER);
		tArchivSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		archivViewer = createArchivViewer(cArchiv);
		archivTable = archivViewer.getTable();
		archivTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));

		Composite cArchivButtonBar = new Composite(cArchiv, SWT.NONE);
		cArchivButtonBar.setLayout(new RowLayout());
		cArchivButtonBar.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 3, 1));

		Button bNewArchiv = new Button(cArchivButtonBar, SWT.NONE);
		bNewArchiv.setLayoutData(new RowData(100, SWT.DEFAULT));
		bNewArchiv.setText("Neuer Eintrag");
		bNewArchiv.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

			}
		});

		Button bLoadArchiv = new Button(cArchivButtonBar, SWT.NONE);
		bLoadArchiv.setLayoutData(new RowData(100, SWT.DEFAULT));
		bLoadArchiv.setText("Eintrag laden");

		Button bRemoveArchiv = new Button(cArchivButtonBar, SWT.NONE);
		bRemoveArchiv.setLayoutData(new RowData(100, SWT.DEFAULT));
		bRemoveArchiv.setText("Eintrag entfernen");
	}

	private TableViewer createArchivViewer(Composite parent) {
		TableViewer archivViewer = new TableViewer(parent, SWT.BORDER | SWT.SINGLE);
		archivViewer.setContentProvider(new ArrayContentProvider());

		// Set visible
		archivViewer.getTable().setHeaderVisible(true);
		archivViewer.getTable().setLinesVisible(true);

		// Columns
		TableViewerColumn viewerColumn = new TableViewerColumn(archivViewer, SWT.LEAD);
		viewerColumn.getColumn().setText("Datum");
		viewerColumn.getColumn().setWidth(120);

		viewerColumn = new TableViewerColumn(archivViewer, SWT.LEAD);
		viewerColumn.getColumn().setText("Titel");
		viewerColumn.getColumn().setWidth(300);

		viewerColumn = new TableViewerColumn(archivViewer, SWT.LEAD);
		viewerColumn.getColumn().setText("Daten");
		viewerColumn.getColumn().setWidth(60);

		archivViewer.setLabelProvider(new ArchivLabelProvider());

		return archivViewer;
	}

	/* ==================================== */
	/* ============== Data Tab ============ */
	/* ==================================== */

	/**
	 * 
	 */
	private void createDataTab() {
		tabCluster = new TabItem(tabFolder, SWT.NONE);
		tabCluster.setText("Daten");
		tabCluster.setImage(getImageDescriptor(ISharedImages.IMG_CLUSTER_16).createImage());

		Composite cCluster = toolkit.createComposite(tabFolder);
		tabCluster.setControl(cCluster);
		cCluster.setLayout(new FillLayout(SWT.HORIZONTAL));

		ScrolledForm formData = toolkit.createScrolledForm(cCluster);
		toolkit.paintBordersFor(formData);
		formData.setText("Daten");
		formData.getBody().setLayout(new GridLayout(1, false));
		
		Composite cTimeline = toolkit.createComposite(formData.getBody(), SWT.NONE);
		toolkit.paintBordersFor(cTimeline);
		cTimeline.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Table dataTable = toolkit.createTable(formData.getBody(), SWT.NONE);
		dataTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		toolkit.paintBordersFor(dataTable);
		dataTable.setHeaderVisible(true);
		dataTable.setLinesVisible(true);
		dataTableViewer = new DataViewer(dataTable);
//		createDataTabToolbar(formData.getToolBarManager());
	}
	
	private void createDataTabToolbar(IToolBarManager toolbar) {
		IMenuService menuService = (IMenuService)getSite().getService(IMenuService.class);
		menuService.populateContributionManager((ToolBarManager)toolbar, DATA_TOOLBAR_CONTRIBUTIONS);
		toolbar.update(true);
	}

	/**
	 * Initalize input
	 */
	private void update() {
/*		IPatientService service = Activator.getPatientService();
		if (service == null) {
			MessageDialog.openError(getSite().getShell(), "Error", "Patient Service Offline");
			return;
		}

		Patient patient = (Patient) service.getSelection(IPatientService.PATIENT);
		if (patient == null)
			return;*/

		// Update Personal Data
/*		tFirstname.setText(patient.getFirstname());
		tLastname.setText(patient.getLastname());
		tGender.setText(String.valueOf(patient.getGender()));
		dBirth.setSelection(patient.getBirth());
		if (patient.getComment() != null)
			tComment.setText(patient.getComment());
*/
		// Update Archiv
/*		EntityManager em = JPAUtil.createEntityManager();
		List<Archiv> archives = em.createNamedQuery("Archiv.findByPatient", Archiv.class).setParameter("patientId", patient.getId())
				.getResultList();
		archivViewer.setInput(archives);*/

	}

	@Override
	public void setFocus() {
		tComment.setFocus();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		update();
	}
}
