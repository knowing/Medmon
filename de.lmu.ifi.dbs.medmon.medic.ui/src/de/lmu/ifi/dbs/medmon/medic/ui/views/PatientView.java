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
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionListener;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.provider.ArchivLabelProvider;
import de.lmu.ifi.dbs.medmon.medic.ui.provider.ISharedImages;

public class PatientView extends ViewPart {

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
	private Text textLastName;
	private Text textFirstname;
	private Text textBirth;
	private Text textGender;
	private Text textInsuranceId;

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

		Composite cPatient = toolkit.createComposite(tabFolder, SWT.NONE);
		tPersonalData.setControl(cPatient);
		cPatient.setLayout(new GridLayout(2, false));
		
		Label lblLastName = new Label(cPatient, SWT.NONE);
		toolkit.adapt(lblLastName, true, true);
		lblLastName.setText("Name:");
		
		textLastName = new Text(cPatient, SWT.BORDER);
		textLastName.setEditable(false);
		textLastName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		toolkit.adapt(textLastName, true, true);
		
		Label lblFirstname = new Label(cPatient, SWT.NONE);
		toolkit.adapt(lblFirstname, true, true);
		lblFirstname.setText("Vorname:");
		
		textFirstname = new Text(cPatient, SWT.BORDER);
		textFirstname.setEditable(false);
		textFirstname.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		toolkit.adapt(textFirstname, true, true);
		
		Label lblBirth = new Label(cPatient, SWT.NONE);
		toolkit.adapt(lblBirth, true, true);
		lblBirth.setText("Geburtsdatum:");
		
		textBirth = new Text(cPatient, SWT.BORDER);
		textBirth.setEditable(false);
		textBirth.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		toolkit.adapt(textBirth, true, true);
		
		Label lblGender = new Label(cPatient, SWT.NONE);
		toolkit.adapt(lblGender, true, true);
		lblGender.setText("Geschlecht:");
		
		textGender = new Text(cPatient, SWT.BORDER);
		textGender.setEditable(false);
		textGender.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		toolkit.adapt(textGender, true, true);
		
		Label lblInsuranceId = new Label(cPatient, SWT.NONE);
		toolkit.adapt(lblInsuranceId, true, true);
		lblInsuranceId.setText("Versicherungsnummer:");
		
		textInsuranceId = new Text(cPatient, SWT.BORDER);
		textInsuranceId.setEditable(false);
		textInsuranceId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		toolkit.adapt(textInsuranceId, true, true);
		
		IGlobalSelectionListener<Patient> selectionListener = new IGlobalSelectionListener<Patient>() {
			
			@Override
			public void selectionChanged(Patient selection) {
				textLastName.setText(selection.getLastname());
				textFirstname.setText(selection.getFirstname());
				textBirth.setText(selection.getBirth().toString());
				textGender.setText("Zwitter");
				textInsuranceId.setText(selection.getInsuranceId());
			}
			
			@Override
			public Class<Patient> getType() {
				return Patient.class;
			}
		};
		
		Activator.getBundleContext().registerService(IGlobalSelectionListener.class, selectionListener, null);
	
/*		
		dBirth = new CDateTime(cPatient, CDT.BORDER | CDT.DATE_SHORT);
		data = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		data.widthHint = 160;
		dBirth.setLayoutData(data);

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

		Composite cArchiv = toolkit.createComposite(tabFolder, SWT.NONE);
		tArchiv.setControl(cArchiv);
		cArchiv.setLayout(new GridLayout(3, false));

		tArchivSearch = new Text(cArchiv, SWT.BORDER);
		tArchivSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

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

	}

	@Override
	public void setFocus() {

	}

}
