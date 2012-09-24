package de.lmu.ifi.dbs.medmon.database.entity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "DATA")
@NamedQueries({
        @NamedQuery(name = "Data.findAll", query = "SELECT d FROM Data d"),
        @NamedQuery(name = "Data.findByPatient", query = "SELECT d FROM Data d WHERE d.patient = :patient"),
        @NamedQuery(name = "Data.findByPatientAndDate", query = "SELECT d FROM Data d WHERE d.patient = :patient AND d.from = :from AND d.to = :to"),
        @NamedQuery(name = "Data.findByPatientAndAfterTo", query = "SELECT d FROM Data d WHERE d.patient = :patient AND d.to < :date"),
        @NamedQuery(name = "Data.findByPatientAndBeforeTo", query = "SELECT d FROM Data d WHERE d.patient = :patient AND d.to > :date"),
        @NamedQuery(name = "Data.findByPatientAndAfterFrom", query = "SELECT d FROM Data d WHERE d.patient = :patient AND d.from < :date"),
        @NamedQuery(name = "Data.findByPatientAndBeforeFrom", query = "SELECT d FROM Data d WHERE d.patient = :patient AND d.from > :date"),
        @NamedQuery(name = "Data.findByPatientAndSensor", query = "SELECT d FROM Data d WHERE d.patient = :patient AND d.sensor = :sensor"),
        @NamedQuery(name = "Data.findEarliestOfPatient", query = "SELECT d FROM Data d WHERE d.patient = :patient AND NOT(EXISTS(SELECT o FROM Data o WHERE o.from < d.from))"),
        @NamedQuery(name = "Data.findLatestOfPatient", query = "SELECT d FROM Data d WHERE d.patient = :patient AND NOT(EXISTS(SELECT o FROM Data o WHERE o.to > d.to))") })
public class Data {

    // TODO Create Enumeration
    public final static String TRAIN = "train";
    public final static String RESULT = "result";
    public final static String RAW = "raw";
    public final static String ROOT = "root";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "FROM_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date from;

    @Column(name = "TO_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date to;

    private String file;

    private String type;

    @OneToOne(cascade = { CascadeType.REMOVE })
    private TherapyResult therapyResult;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Sensor sensor;

    @ManyToOne
    private Patient patient;

    protected Data() {
    }

    public Data(Date from, Date to, Sensor sensor) {
        setFrom(from);
        setTo(to);
        setSensor(sensor);
    }

    public Data(TherapyResult therapyResult, Sensor sensor) {
        setTherapyResult(therapyResult);
        setSensor(sensor);
    }

    @PrePersist
    void prePersist() {
        // is absolute? normalize?
        Path dataFile = patient.toPath().resolve(type).resolve(generateFilename());
        setFile(dataFile.toString());
        try {
            // Always override file
            Files.deleteIfExists(dataFile);
            Files.createFile(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PreRemove
    void preRemove() {
        sensor.removeData(this);
        if (patient == null && patient.remove)
            return;

        patient.removeData(this);
        try {
            Files.deleteIfExists(Paths.get(getFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateFilename() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss-SSS");
        StringBuilder sb = new StringBuilder(32);
        sb.append(df.format(from)).append("_to_").append(df.format(to)).append(".");
        switch (type) {
        case RAW:
            return sb.append(sensor.getId()).append(".").append(sensor.getFilePrefix()).toString();
        case TRAIN:
            return sb.append("arff").toString();
        case RESULT:
            return sb.append("arff").toString();
        default:
            return sb.append("unkown").toString();
        }
    }

    public Path toPath() {
        return Paths.get(file);
    }

    public long getId() {
        return id;
    }

    protected void setId(long id) {
        this.id = id;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public TherapyResult getTherapyResult() {
        return therapyResult;
    }

    public void setTherapyResult(TherapyResult therapyResult) {
        this.therapyResult = therapyResult;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
        if (sensor != null)
            sensor.addData(this);
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    @Override
    public String toString() {
        return "Data [id=" + id + ", from=" + from + ", to=" + to + ", file=" + file + ", type=" + type + ", therapyResult="
                + (therapyResult == null ? "null" : therapyResult.getId()) + ", sensor=" + sensor.getId() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Data other = (Data) obj;
        if (id != other.id)
            return false;
        return true;
    }

}