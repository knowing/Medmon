package de.lmu.ifi.dbs.medmon.medic.ui.wizard;

import static de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizardOptions.IMPORT_RAW;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;

import de.lmu.ifi.dbs.medmon.base.ui.wizard.pages.ImportDataSensorAndDirectoryPage;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.ImportDataPatientAndTypePage;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;
import de.lmu.ifi.dbs.medmon.services.IPatientService;

public class ImportDataWizard extends Wizard {

    private ImportDataPatientAndTypePage patientAndTypePage;
    private ImportDataSensorAndDirectoryPage sensorAndDirectoryPage;

    public ImportDataWizard() {
        setWindowTitle("New Wizard");
    }

    @Override
    public void addPages() {
        addPage(patientAndTypePage = new ImportDataPatientAndTypePage());
        addPage(sensorAndDirectoryPage = new ImportDataSensorAndDirectoryPage());
    }

    @Override
    public boolean performFinish() {
        Patient selectedPatient = patientAndTypePage.getSelectedPatient();
        ISensor selectedSensor = sensorAndDirectoryPage.getSelectedSensor();

        IPatientService patientService = Activator.getPatientService();

        int options = patientAndTypePage.getOption();
        if ((options & IMPORT_RAW) != 0) {

            // Copy selected file
            if (sensorAndDirectoryPage.isFileSectionEnabled()) {
                String selectedFile = sensorAndDirectoryPage.getSelectedFile();
                try (InputStream in = Files.newInputStream(Paths.get(selectedFile))) {
                    throw new RuntimeException("Raw data selectedFile import not implemented yet");
                } catch (IOException e) {
                    MessageDialog.openError(getShell(), "Daten konnten nicht importiert werden", e.getMessage());
                    e.printStackTrace();
                }

                // Convert to instances and store
            } else {
                try (InputStream in = selectedSensor.getDataInputStream()) {
                    throw new RuntimeException("Raw data sensor import not implemented yet");
                } catch (IOException e) {
                    MessageDialog.openError(getShell(), "Daten konnten nicht importiert werden", e.getMessage());
                    e.printStackTrace();
                }
            }

            return true;
        }
        return false;
    }

}
