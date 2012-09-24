package de.lmu.ifi.dbs.medmon.sensor.core;

/**
 * Simple event for sensor changes
 * 
 * @author Nepomuk Seiler
 * 
 */
public class SensorEvent {

    public final ISensor sensor;

    public SensorEvent(ISensor sensor) {
        this.sensor = sensor;
    }

}
