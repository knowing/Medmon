package de.lmu.ifi.dbs.medmon.medic.ui.views;

import static de.lmu.ifi.dbs.medmon.medic.ui.Activator.getImageDescriptor;

import java.util.Date;

import javax.persistence.EntityManager;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.base.ui.viewer.DataViewer;
import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionListener;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.provider.ISharedImages;
import org.eclipse.swt.widgets.Link;

public class PatientView extends ViewPart {
	public PatientView() {
	}

	public static final String			ID								= "de.lmu.ifi.dbs.medmon.medic.ui.PatientView";				//$NON-NLS-1$
	private static final String			PATIENT_TOOLBAR_CONTRIBUTIONS	= "toolbar:de.lmu.ifi.dbs.medmon.medic.ui.PatientView.Patient";
	private static final String			THERAPY_TOOLBAR_CONTRIBUTIONS	= "toolbar:de.lmu.ifi.dbs.medmon.medic.ui.PatientView.Archiv";	// Refractor
																																		// ?!?!?
	private static final String			DATA_TOOLBAR_CONTRIBUTIONS		= "toolbar:de.lmu.ifi.dbs.medmon.medic.ui.PatientView.Data";

	private static final Logger			log								= LoggerFactory.getLogger(Activator.PLUGIN_ID);
	private TabFolder					tabFolder;

	/* Personal Data */
	private Text						tLastname, tFirstname, tGender, tComment;
	private CDateTime					dBirth;
	private Table						therapyTable;
	private TableViewer					therapyViewer;
	private TabItem						tabCluster;

	private FormToolkit					toolkit;
	private TableViewer					dataTableViewer;
	private Text						textLastName;
	private Text						textFirstname;
	private Text						textInsuranceId;
	private Button						btnMale;
	private Button						btnFemale;
	private CDateTime					dateBirth;

	private PatientFileDetailBlock		patientFileDetailBlock;
	private Text						text;
	private IGlobalSelectionProvider	selectionProvider;
	private EntityManager				entityManager;
	private TaskSeriesCollection		dataset;
	private JFreeChart					chart;

	/**
	 * @wbp.nonvisual location=82,149
	 */

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
		entityManager = JPAUtil.createEntityManager();

		toolkit = new FormToolkit(parent.getDisplay());
		Composite container = toolkit.createComposite(parent);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));

		tabFolder = new TabFolder(container, SWT.BOTTOM);
		toolkit.adapt(tabFolder, true, true);

		createPersonalTab();
		createTherapyTab();
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
		cPatient.setLayout(new GridLayout(4, false));

		Label lblLastName = new Label(cPatient, SWT.NONE);
		toolkit.adapt(lblLastName, true, true);
		lblLastName.setText("Name:");

		textLastName = new Text(cPatient, SWT.BORDER);
		textLastName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		toolkit.adapt(textLastName, true, true);

		Label lblFirstname = new Label(cPatient, SWT.NONE);
		toolkit.adapt(lblFirstname, true, true);
		lblFirstname.setText("Vorname:");

		textFirstname = new Text(cPatient, SWT.BORDER);
		textFirstname.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		toolkit.adapt(textFirstname, true, true);

		Label lblBirth = new Label(cPatient, SWT.NONE);
		toolkit.adapt(lblBirth, true, true);
		lblBirth.setText("Geburtsdatum:");

		dateBirth = new CDateTime(cPatient, CDT.BORDER | CDT.SPINNER | CDT.DROP_DOWN | CDT.DATE_MEDIUM);
		GridData gd_dateBirth = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		gd_dateBirth.heightHint = 20;
		dateBirth.setLayoutData(gd_dateBirth);
		toolkit.adapt(dateBirth);
		toolkit.paintBordersFor(dateBirth);

		Label lblPlaceholder1 = toolkit.createLabel(cPatient, "", SWT.NONE);
		lblPlaceholder1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 2));

		Label lblGender = new Label(cPatient, SWT.NONE);
		toolkit.adapt(lblGender, true, true);
		lblGender.setText("Geschlecht:");

		btnFemale = toolkit.createButton(cPatient, "weiblich", SWT.RADIO);
		GridData gd_btnFemale = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnFemale.heightHint = 20;
		btnFemale.setLayoutData(gd_btnFemale);

		btnMale = toolkit.createButton(cPatient, "m\u00E4nnlich", SWT.RADIO);
		GridData gd_btnMale = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnMale.heightHint = 20;
		btnMale.setLayoutData(gd_btnMale);

		Label lblInsuranceId = new Label(cPatient, SWT.NONE);
		toolkit.adapt(lblInsuranceId, true, true);
		lblInsuranceId.setText("Versicherungsnummer:");

		textInsuranceId = new Text(cPatient, SWT.BORDER);
		textInsuranceId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		toolkit.adapt(textInsuranceId, true, true);

		Label lblPlaceholder2 = new Label(cPatient, SWT.NONE);
		lblPlaceholder2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 4, 1));
		toolkit.adapt(lblPlaceholder2, true, true);

		Composite composite = new Composite(cPatient, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1));
		toolkit.adapt(composite);
		toolkit.paintBordersFor(composite);

		Label lblPlaceholder3 = new Label(composite, SWT.NONE);
		lblPlaceholder3.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		toolkit.adapt(lblPlaceholder3, true, true);

		Link linkSave = new Link(composite, SWT.NONE);
		toolkit.adapt(linkSave, true, true);
		linkSave.setText("<a>speichern</a>");
		linkSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Patient selection = selectionProvider.getSelection(Patient.class);

				if (selection == null)
					return;

				/************************************************************
				 * Database Access Begin
				 ************************************************************/

				entityManager.getTransaction().begin();
				Patient mPatient = entityManager.merge(selection);

				mPatient.setFirstname(textFirstname.getText());
				mPatient.setLastname(textLastName.getText());
				mPatient.setBirth(dateBirth.getSelection());

				if (btnMale.getSelection())
					mPatient.setGender((short) 0);
				else
					mPatient.setGender((short) 1);

				entityManager.getTransaction().commit();
				entityManager.detach(mPatient);

				/************************************************************
				 * Database Access End
				 ************************************************************/

				getViewSite().getActionBars().getStatusLineManager().setMessage("Patientendaten wurde gespeichert");
			}
		});

		/************************************************************
		 * Listener for Patient.class
		 ************************************************************/
		selectionProvider.registerSelectionListener(new IGlobalSelectionListener<Patient>() {

			@Override
			public void selectionChanged(Patient selection) {

				/************************************************************
				 * selection != null
				 ************************************************************/
				if (selection != null) {

					/************************************************************
					 * Fill the UI components
					 ************************************************************/

					textLastName.setText(selection.getLastname());
					textFirstname.setText(selection.getFirstname());
					dateBirth.setSelection(selection.getBirth());
					switch (selection.getGender()) {
					case 0:
						btnFemale.setSelection(false);
						btnMale.setSelection(true);
						break;
					case 1:
						btnFemale.setSelection(true);
						btnMale.setSelection(false);
						break;
					}
					textInsuranceId.setText(selection.getInsuranceId());

					/************************************************************
					 * fill JFreechart
					 ************************************************************/

					entityManager.getTransaction().begin();
					Patient mPatient = entityManager.merge(selection);
					entityManager.getTransaction().commit();

					final TaskSeries series = new TaskSeries("");

					// should look up therapy start and end
					Task mainTask = new Task("", new Date(2000, 1, 1), new Date(2000, 1, 10));

					for (Data data : selection.getData()) {
						mainTask.addSubtask(new Task("Sensor" + data.getSensor().getId(), data.getFrom(), data.getTo()));
					}

					mainTask.addSubtask(new Task("test1", new Date(2000, 1, 1), new Date(2000, 1, 2)));
					mainTask.addSubtask(new Task("test2", new Date(2000, 1, 3), new Date(2000, 1, 5)));
					series.add(mainTask);

					dataset.removeAll();
					dataset.add(series);

				}
				/************************************************************
				 * selection == null
				 ************************************************************/
				else {
					/************************************************************
					 * fill all UI components with blank strings
					 ************************************************************/
					textLastName.setText("");
					textFirstname.setText("");
					dateBirth.setSelection(new Date());
					btnFemale.setSelection(false);
					btnMale.setSelection(true);
					textInsuranceId.setText("");
				}

			}

			@Override
			public void selectionUpdated() {
				// TODO Auto-generated method stub

			}

			@Override
			public Class<Patient> getType() {
				return Patient.class;
			}
		});
		/************************************************************
		 * Listener END
		 ************************************************************/

		/*
		 * dBirth = new CDateTime(cPatient, CDT.BORDER | CDT.DATE_SHORT); data =
		 * new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		 * data.widthHint = 160; dBirth.setLayoutData(data);
		 */
	}

	/* ==================================== */
	/* ============= Therapy Tab =========== */
	/* ==================================== */
	/**
	 * 
	 */
	private void createTherapyTab() {

		TabItem tTherapy = new TabItem(tabFolder, SWT.NONE);
		tTherapy.setText("Therapien");
		tTherapy.setImage(getImageDescriptor(ISharedImages.IMG_COMMENT_16).createImage());

		Composite cTherapy = toolkit.createComposite(tabFolder, SWT.NONE);
		tTherapy.setControl(cTherapy);
		cTherapy.setLayout(new FillLayout());

		patientFileDetailBlock = new PatientFileDetailBlock();
		patientFileDetailBlock.createContent(new ManagedForm(cTherapy));

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

		dataset = new TaskSeriesCollection();
		chart = ChartFactory.createGanttChart(null, null, null, dataset, false, true, false);
		ChartComposite chartComposite = new ChartComposite(formData.getBody(), SWT.NONE, chart);

		chartComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		toolkit.adapt(chartComposite);
		toolkit.paintBordersFor(chartComposite);

		Composite cTimeline = toolkit.createComposite(formData.getBody(), SWT.NONE);
		toolkit.paintBordersFor(cTimeline);
		cTimeline.setLayout(new FillLayout(SWT.HORIZONTAL));

		text = new Text(formData.getBody(), SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		toolkit.adapt(text, true, true);

		Table dataTable = toolkit.createTable(formData.getBody(), SWT.NONE);
		dataTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		toolkit.paintBordersFor(dataTable);
		dataTable.setHeaderVisible(true);
		dataTable.setLinesVisible(true);
		dataTableViewer = new DataViewer(dataTable);
		// createDataTabToolbar(formData.getToolBarManager());
	}

	private void createDataTabToolbar(IToolBarManager toolbar) {
		IMenuService menuService = (IMenuService) getSite().getService(IMenuService.class);
		menuService.populateContributionManager((ToolBarManager) toolbar, DATA_TOOLBAR_CONTRIBUTIONS);
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

	@Override
	public void dispose() {
		selectionProvider.unregister();
		entityManager.close();
		super.dispose();
	}
}
