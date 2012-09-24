package de.lmu.ifi.dbs.medmon.medic.ui.views;

import static de.lmu.ifi.dbs.medmon.medic.ui.Activator.getImageDescriptor;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.RollbackException;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
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
import de.lmu.ifi.dbs.medmon.database.entity.Data;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.database.entity.Sensor;
import de.lmu.ifi.dbs.medmon.database.entity.TherapyResult;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.provider.ISharedImages;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;
import de.lmu.ifi.dbs.medmon.services.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.services.IGlobalSelectionListener;
import de.lmu.ifi.dbs.medmon.services.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.services.ISensorObserver;

public class PatientView extends ViewPart {
    public PatientView() {
    }

    public static final String ID = "de.lmu.ifi.dbs.medmon.medic.ui.PatientView"; //$NON-NLS-1$
    private static final String PATIENT_TOOLBAR_CONTRIBUTIONS = "toolbar:de.lmu.ifi.dbs.medmon.medic.ui.PatientView.Patient";
    private static final String THERAPY_TOOLBAR_CONTRIBUTIONS = "toolbar:de.lmu.ifi.dbs.medmon.medic.ui.PatientView.Archiv"; // Refractor
                                                                                                                             // ?!?!?
    private static final String DATA_TOOLBAR_CONTRIBUTIONS = "toolbar:de.lmu.ifi.dbs.medmon.medic.ui.PatientView.Data";

    private static final Logger log = LoggerFactory.getLogger(Activator.PLUGIN_ID);
    private TabFolder tabFolder;

    /* Personal Data */
    private Text tLastname, tFirstname, tGender, tComment;
    private CDateTime dBirth;
    private Table therapyTable;
    private TableViewer therapyViewer;
    private TabItem tabCluster;

    private FormToolkit toolkit;
    private TableViewer dataTableViewer;
    private Text textLastName;
    private Text textFirstname;
    private Text textInsuranceId;
    private Button btnMale;
    private Button btnFemale;
    private CDateTime dateBirth;

    private PatientFileDetailBlock patientFileDetailBlock;
    private IGlobalSelectionProvider selectionProvider;
    private EntityManager workerEM;
    private TaskSeriesCollection dataset;
    private JFreeChart chart;
    private Table dataTable;
    private ComboViewer comboViewer;

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
        workerEM = Activator.getEntityManagerService().createEntityManager();

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

    /************************************************************
     * Personal Tab
     ************************************************************/
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

        dateBirth = new CDateTime(cPatient, CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM);
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

                workerEM.getTransaction().begin();
                Patient mPatient = workerEM.find(Patient.class, selection.getId());

                mPatient.setFirstname(textFirstname.getText());
                mPatient.setLastname(textLastName.getText());
                mPatient.setBirth(dateBirth.getSelection());

                if (btnMale.getSelection())
                    mPatient.setGender((short) 0);
                else
                    mPatient.setGender((short) 1);

                workerEM.getTransaction().commit();
                workerEM.clear();

                /************************************************************
                 * Database Access End
                 ************************************************************/

                getViewSite().getActionBars().getStatusLineManager().setMessage("Patientendaten wurde gespeichert");
            }
        });

        selectionProvider.registerSelectionListener(new IGlobalSelectionListener<Patient>() {
            private Patient selectedPatient;

            @SuppressWarnings("unchecked")
            @Override
            public void selectionChanged(Patient selection) {

                /************************************************************
                 * selection != null
                 ************************************************************/
                if (selection != null) {

                    selectedPatient = selection;

                    /************************************************************
                     * Fill the UI components
                     ************************************************************/

                    Patient mPatient = workerEM.find(Patient.class, selectedPatient.getId());
                    textLastName.setText(mPatient.getLastname());
                    textFirstname.setText(mPatient.getFirstname());
                    dateBirth.setSelection(mPatient.getBirth());
                    switch (mPatient.getGender()) {
                    case 0:
                        btnFemale.setSelection(false);
                        btnMale.setSelection(true);
                        break;
                    case 1:
                        btnFemale.setSelection(true);
                        btnMale.setSelection(false);
                        break;
                    }
                    textInsuranceId.setText(mPatient.getInsuranceId());
                    workerEM.clear();
                }
                /************************************************************
                 * selection == null
                 ************************************************************/
                else {
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
                if (selectedPatient == null)
                    return;
                selectionChanged(selectedPatient);
            }

            @Override
            public Class<Patient> getType() {
                return Patient.class;
            }
        });
        /************************************************************
         * Listener END
         ************************************************************/
    }

    /************************************************************
     * create the Therapy Tab
     ************************************************************/
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

    /************************************************************
     * Data Tab
     ************************************************************/
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

        chartComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        toolkit.adapt(chartComposite);
        toolkit.paintBordersFor(chartComposite);

        Composite cTimeline = toolkit.createComposite(formData.getBody(), SWT.NONE);
        cTimeline.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        toolkit.paintBordersFor(cTimeline);
        cTimeline.setLayout(new FillLayout(SWT.HORIZONTAL));

        comboViewer = new ComboViewer(formData.getBody(), SWT.NONE);
        final Combo comboFilter = comboViewer.getCombo();
        comboFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        toolkit.paintBordersFor(comboFilter);
        comboFilter.addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyEvent e) {
                dataTableViewer.refresh(false);
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }
        });
        comboFilter.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dataTableViewer.refresh(false);
            }
        });
        comboViewer.setContentProvider(ArrayContentProvider.getInstance());
        comboViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((ISensor) element).getName();
            }
        });

        // FIXME this is outdated
        ISensorObserver sensorObserver = new ISensorObserver() {
            private List<ISensor> localSensorList = new LinkedList<ISensor>();

            @Override
            public void init(List<ISensor> sensors) {
                if (comboViewer.getControl().isDisposed())
                    return;
                localSensorList.addAll(sensors);
                comboViewer.setInput(localSensorList);
            }

            @Override
            public void sensorAdded(ISensor service) {
                if (comboViewer.getControl().isDisposed())
                    return;
                localSensorList.add(service);
                comboViewer.refresh();
            }

            @Override
            public void sensorRemoved(ISensor service) {
                if (comboViewer.getControl().isDisposed())
                    return;
                localSensorList.remove(service);
                comboViewer.refresh();
            }

            @Override
            public void sensorUpdated(ISensor service) {
                if (comboViewer.getControl().isDisposed())
                    return;
                comboViewer.refresh();
            }

        };
        Activator.getBundleContext().registerService(ISensorObserver.class, sensorObserver, null);

        dataTable = toolkit.createTable(formData.getBody(), SWT.FULL_SELECTION);
        dataTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        toolkit.paintBordersFor(dataTable);
        dataTable.setHeaderVisible(true);
        dataTable.setLinesVisible(true);
        dataTableViewer = new DataViewer(dataTable);
        dataTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) dataTableViewer.getSelection();
                if (selection.isEmpty())
                    return;
                selectionProvider.setSelection(Data.class, (Data) selection.getFirstElement());
            }
        });
        dataTableViewer.addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                String filter = comboFilter.getText().toLowerCase();
                Data d = (Data) element;
                if (d.getSensor().getName().toLowerCase().contains(filter))
                    return true;
                return false;
            }
        });

        Menu popUpMenu = new Menu(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.POP_UP);
        MenuItem itemDelete = new MenuItem(popUpMenu, SWT.PUSH);
        itemDelete.setText("l\u00F6schen");
        itemDelete.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Data data = selectionProvider.getSelection(Data.class);
                if (data == null)
                    return;
                try {
                    EntityManager tempEM = Activator.getEntityManagerService().createEntityManager();
                    tempEM.getTransaction().begin();
                    Data mData = tempEM.find(Data.class, data.getId());
                    tempEM.remove(mData);
                    tempEM.getTransaction().commit();
                    tempEM.close();

                    selectionProvider.updateSelection(Patient.class);
                    selectionProvider.setSelection(Data.class, null);
                } catch (RollbackException exc) {
                    MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                            "Daten k\u00F6nnen nicht gel\u00F6scht werden", "Sie m\u00fcssen erst das Therapieergebnis l\u00F6schen");
                }

            }
        });
        dataTable.setMenu(popUpMenu);

        selectionProvider.registerSelectionListener(new IGlobalSelectionListener<Patient>() {
            private Patient selectedPatient;

            @SuppressWarnings("unchecked")
            @Override
            public void selectionChanged(Patient selection) {

                /************************************************************
                 * selection != null
                 ************************************************************/
                if (selection != null) {

                    selectedPatient = selection;
                    /************************************************************
                     * fill TableViewer
                     ************************************************************/
                    Patient mPatient = workerEM.find(Patient.class, selection.getId());
                    Query allDataQuery = workerEM.createNamedQuery("Data.findByPatient");
                    List<Data> allData = allDataQuery.setParameter("patient", mPatient).getResultList();
                    dataTableViewer.setInput(allData);

                    /************************************************************
                     * fill JFreechart
                     ************************************************************/
                    TaskSeries series = new TaskSeries(new String());
                    if (allData.size() > 0) {

                        Query leftBoundsQuery = workerEM.createNamedQuery("Data.findEarliestOfPatient");
                        Query rightBoundsQuery = workerEM.createNamedQuery("Data.findLatestOfPatient");

                        Data leftBounds = (Data) leftBoundsQuery.setParameter("patient", mPatient).getResultList().get(0);
                        Data rightBounds = (Data) rightBoundsQuery.setParameter("patient", mPatient).getResultList().get(0);

                        Query allSensorsQuery = workerEM.createNamedQuery("Sensor.findByPatient");
                        List<Sensor> allSensors = allSensorsQuery.setParameter("patient", mPatient).setParameter("patient", mPatient)
                                .getResultList();

                        for (Sensor sensor : allSensors) {
                            Task task = new Task(sensor.getName(), leftBounds.getFrom(), rightBounds.getTo());
                            Query dataFromSensorQuery = workerEM.createNamedQuery("Data.findByPatientAndSensor");
                            List<Data> dataFromSensor = dataFromSensorQuery.setParameter("patient", mPatient)
                                    .setParameter("sensor", sensor).getResultList();
                            for (Data data : dataFromSensor) {
                                Task subTask = new Task("Sensor" + data.getSensor().getId(), data.getFrom(), data.getTo());
                                TherapyResult therapyResult = data.getTherapyResult();
                                if (therapyResult != null) {
                                    subTask.setPercentComplete((double) therapyResult.getSuccess() / 100);
                                }
                                task.addSubtask(subTask);
                            }
                            series.add(task);
                        }
                    }
                    dataset.removeAll();
                    dataset.add(series);
                    workerEM.clear();

                }
                /************************************************************
                 * selection == null
                 ************************************************************/
                else {

                    dataTableViewer.setInput(null);
                    dataset.removeAll();
                }
            }

            @Override
            public void selectionUpdated() {
                if (selectedPatient == null)
                    return;
                selectionChanged(selectedPatient);
            }

            @Override
            public Class<Patient> getType() {
                return Patient.class;
            }
        });
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
        workerEM.close();
        super.dispose();
    }
}
