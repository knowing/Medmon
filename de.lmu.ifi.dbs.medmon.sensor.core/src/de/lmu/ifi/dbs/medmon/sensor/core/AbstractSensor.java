package de.lmu.ifi.dbs.medmon.sensor.core;


public abstract class AbstractSensor implements ISensor {

	private final String	id;
	
	public AbstractSensor() {
		id = getClass().getName() + getVersion();
	}

	@Override
	public String getId() {
		return id;
	}

}
