package de.lmu.ifi.dbs.medmon.base.ui.wizard.pages;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.joda.time.Interval;

import de.lmu.ifi.dbs.medmon.base.ui.viewer.SensorTableViewer;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.IValidationPage;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.ValidationListener;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensorDriver;

public class ImportDataSensorAndDirectoryPage extends WizardPage implements IValidationPage {

    private static final String ERROR_NO_FILE_CHOOSEN = "Keine Sensordatei ausgew\u00e4hlt";
    private static final String ERROR_NO_SENSOR_SELECTED = "Kein Sensor ausgew\u00e4hlt";
    private static final String ERROR_WRONG_FILE = "Der Sensor kann mit der ausgew\u00e4hlten Datei nicht umgehen.";

    private ISensor selectedSensor = null;
    private boolean isFileSectionEnabled = false;
    private SortedSet<String> errors = new TreeSet<String>();

    private Button btnChooseDirectory;
    private SensorTableViewer sensorTableViewer;
    private Text txtSelectedFile;

    /**
     * Create the wizard.
     */
    public ImportDataSensorAndDirectoryPage() {
        super("ImportDataSensorAndDirectoryPage");
        setTitle("Sensor ausw\u00e4hlen");
        setDescription("W\u00e4hlen Sie einen Sensor aus von dem Sie die Daten verwenden m\u00f6chten.");
    }

    /**
     * Create contents of the wizard.
     */
    public void createControl(Composite parent) {

        Composite container = new Composite(parent, SWT.NULL);
        setControl(container);
        container.setLayout(new GridLayout(2, false));

        sensorTableViewer = new SensorTableViewer(container, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
        Table table = sensorTableViewer.getTable();
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        table.addSelectionListener(new ValidationListener(this) {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) sensorTableViewer.getSelection();
                if (!selection.isEmpty()) {
                    selectedSensor = (ISensor) selection.getFirstElement();
                    isFileSectionEnabled = !selectedSensor.isInstance();
                    btnChooseDirectory.setEnabled(isFileSectionEnabled);
                } else {
                    selectedSensor = null;
                }
                checkContents();
            }
        });

        txtSelectedFile = new Text(container, SWT.BORDER);
        txtSelectedFile.setEditable(false);
        txtSelectedFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        btnChooseDirectory = new Button(container, SWT.NONE);
        btnChooseDirectory.setText("Durchsuchen");
        btnChooseDirectory.setEnabled(false);
        btnChooseDirectory.addSelectionListener(new ValidationListener(this) {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dlg = new FileDialog(getShell());
                dlg.setText("Sensordatei");
                String selectedFile = dlg.open();
                if (selectedFile != null)
                    txtSelectedFile.setText(selectedFile);
                else
                    txtSelectedFile.setText("");

                checkContents();
            }
        });

        checkContents();
    }

    @Override
    public void checkContents() {

        // selectedSensor = JFaceUtil.initializeViewerSelection(ISensor.class, sensorTableViewer);
        // Check file
        if (isFileSectionEnabled) {
            if (txtSelectedFile.getText() == null || txtSelectedFile.getText().isEmpty())
                errors.add(ERROR_NO_FILE_CHOOSEN);
            else
                errors.remove(ERROR_NO_FILE_CHOOSEN);
        } else
            errors.remove(ERROR_NO_FILE_CHOOSEN);

        // Check sensor
        if (selectedSensor == null)
            errors.add(ERROR_NO_SENSOR_SELECTED);
        else
            errors.remove(ERROR_NO_SENSOR_SELECTED);

        // Check file correctness
        if (errors.isEmpty()) {
            try (InputStream in = Files.newInputStream(Paths.get(txtSelectedFile.getText()))) {
                ISensorDriver driver = selectedSensor.getDriver();
                Interval interval = driver.getInterval(in);
                errors.remove(ERROR_WRONG_FILE);
                DateFormat df = DateFormat.getDateTimeInstance();
                setMessage(df.format(interval.getStart().toDate()) + " - " + df.format(interval.getEnd().toDate()));
            } catch (IOException e) {
                errors.add(ERROR_WRONG_FILE);
            }
        }

        // Setting UI
        if (errors.isEmpty()) {
            setErrorMessage(null);
            setPageComplete(true);
        } else {
            setErrorMessage(errors.first());
            errors.clear();
            setPageComplete(false);
        }
    }

    /**
     * WIZZARD-GET: get selected Sensor
     */
    public ISensor getSelectedSensor() {
        return selectedSensor;
    }

    /**
     * WIZZARD-GET: get selected Directory
     */
    public String getSelectedFile() {
        return txtSelectedFile.getText();
    }

    public boolean isFileSectionEnabled() {
        return isFileSectionEnabled;
    }

}
