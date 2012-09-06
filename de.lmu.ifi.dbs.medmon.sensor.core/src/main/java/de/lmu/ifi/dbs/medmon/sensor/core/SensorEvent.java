package de.lmu.ifi.dbs.medmon.sensor.core;

public class SensorEvent {

    public final ISensor sensor;
    public final boolean available;
    
    public SensorEvent(ISensor sensor, boolean available) {
        this.sensor = sensor;
        this.available = available;
    }
    
}
