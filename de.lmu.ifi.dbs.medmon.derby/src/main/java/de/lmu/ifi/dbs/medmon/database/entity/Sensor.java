package de.lmu.ifi.dbs.medmon.database.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

@Entity
@NamedQueries({
        @NamedQuery(name = "Sensor.findAll", query = "SELECT s FROM Sensor s"),
        @NamedQuery(name = "Sensor.findByPatient", query = "SELECT s FROM Sensor s WHERE EXISTS( SELECT d FROM Data d WHERE d.sensor = s AND d.patient = :patient)") })
public class Sensor {

    @Id
    private String id;

    private String name;

    private String serial;

    private String defaultpath;

    private String filePrefix;

    @OneToMany(mappedBy = "sensor", cascade = { CascadeType.REMOVE })
    private List<Data> data;

    protected Sensor() {
    }

    public Sensor(String id, String name, String serial) {
        this.id = id;
        this.name = name;
        this.serial = serial;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerial() {
        return serial;
    }

    public void getSerial(String serial) {
        this.serial = serial;
    }

    public String getDefaultpath() {
        return defaultpath;
    }

    public void setDefaultpath(String defaultpath) {
        this.defaultpath = defaultpath;
    }

    public List<Data> getData() {
        return data;
    }

    protected void setData(List<Data> data) {
        this.data = data;
    }

    public boolean addData(Data d) {
        if (data == null)
            data = new ArrayList<>();
        if (data.contains(d))
            return false;
        boolean success = data.add(d);
        if (!this.equals(d.getSensor()))
            d.setSensor(this);
        return success;
    }

    public boolean removeData(Data data) {
        if (data == null)
            this.data = new ArrayList<>();
        return this.data.remove(data);
    }

    public String getFilePrefix() {
        return filePrefix;
    }

    public void setFilePrefix(String filePrefix) {
        this.filePrefix = filePrefix;
    }

    @Override
    public String toString() {
        return "Sensor [id=" + id + ", name=" + name + ", serial=" + serial + ", defaultpath=" + defaultpath + ", filePrefix=" + filePrefix
                + ", data=" + data + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        Sensor other = (Sensor) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
