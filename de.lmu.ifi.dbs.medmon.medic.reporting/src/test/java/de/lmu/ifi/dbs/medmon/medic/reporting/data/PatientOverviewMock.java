package de.lmu.ifi.dbs.medmon.medic.reporting.data;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.joda.time.DateTime;

import weka.core.Instances;
import de.lmu.ifi.dbs.medmon.database.entity.Data;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.database.entity.Sensor;
import de.lmu.ifi.dbs.medmon.database.entity.Therapy;
import de.lmu.ifi.dbs.medmon.database.entity.TherapyResult;

public class PatientOverviewMock {

    private final Patient patient;
    private final Therapy therapy;

    public PatientOverviewMock() {
        this.patient = createPatient();
        this.therapy = createTherapy(patient);
    }

    public Patient getPatient() {
        return patient;
    }

    public Therapy getTherapy() {
        return therapy;
    }

    public Instances getTimeSeriesResults() {
        BarChartMock results = new BarChartMock();
        return results.getInstances();
    }

    public Instances getTimeSeriesResults(TherapyResult result) {
        Path file = result.getData().toPath();
        BarChartMock results = new BarChartMock();
        return results.getInstances();
    }

    private Patient createPatient() {
        Patient patient = new Patient();
        patient.setId(12345);
        patient.setFirstname("Max");
        patient.setLastname("Mustermann");
        patient.setComment("This is a sample comment");
        patient.setGender(Patient.MALE);
        patient.setInsuranceId("123-ABC-30");
        patient.setBirth(new DateTime(1990, 12, 12, 0, 0).toDate());

        return patient;
    }

    private Therapy createTherapy(Patient patient) {
        Therapy therapy = new Therapy("Sample Therapy", patient);

        DateTime dateTime = new DateTime(2012, 2, 1, 0, 0);
        therapy.setComment("Therapy sample comment");
        therapy.setTherapyStart(dateTime.toDate());
        therapy.setTherapyEnd(dateTime.plusDays(14).toDate());

        therapy.setTherapyStart(new Date());
        therapy.setTherapyEnd(new Date());
        addTherapyResults(therapy);

        return therapy;
    }

    private void addTherapyResults(Therapy therapy) {
        Sensor sensor = new Sensor("3D Sensor", "A1", "1.0", "txt");
        DateTime dateTime = new DateTime(2012, 2, 1, 0, 0);

        Path root = Paths.get("home", "user", ".medmon", "patients", "00000001");

        Data d1 = new Data(dateTime.toDate(), dateTime.plusDays(3).toDate(), sensor);
        d1.setFile(root.resolve("01.arff").toString());
        TherapyResult result = new TherapyResult("First Result", d1, therapy);
        result.setTimestamp(dateTime.plusDays(15).toDate());

        Data d2 = new Data(dateTime.plusDays(4).toDate(), dateTime.plusDays(7).toDate(), sensor);
        d2.setFile(root.resolve("02").toString());
        result = new TherapyResult("Second Result", d2, therapy);
        result.setTimestamp(dateTime.plusDays(15).toDate());

        Data d3 = new Data(dateTime.plusDays(8).toDate(), dateTime.plusDays(11).toDate(), sensor);
        d3.setFile(root.resolve("03.arff").toString());
        result = new TherapyResult("Third Result", d3, therapy);
        result.setTimestamp(dateTime.plusDays(15).toDate());

        Data d4 = new Data(dateTime.plusDays(12).toDate(), dateTime.plusDays(14).toDate(), sensor);
        d4.setFile(root.resolve("04.arff").toString());
        result = new TherapyResult("Fourth Result", d4, therapy);
        result.setTimestamp(dateTime.plusDays(15).toDate());
    }

}
