package de.lmu.ifi.dbs.medmon.sensor.core;

import java.io.IOException;

import org.joda.time.Interval;

import weka.core.Instances;

public abstract class AbstractSensor implements ISensor {

    private final String id;
    private ISensorDriver driver;
    protected boolean instance;

    public AbstractSensor() {
        id = getClass().getName() + "_" + getSerial();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ISensorDriver getDriver() {
        return driver;
    }

    @Override
    public void setDriver(ISensorDriver driver) {
        this.driver = driver;
    }

    @Override
    public void noDriverFound() {
        this.driver = null;
    }

    @Override
    public Instances getData() throws IOException {
        return getDriver().getData(getDataInputStream());
    }

    @Override
    public Interval getInterval() throws IOException {
        return getDriver().getInterval(getDataInputStream());
    }

    @Override
    public boolean isInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return getName() + " - " + getSerial() + " [" + getId() + "]";
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
        AbstractSensor other = (AbstractSensor) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
