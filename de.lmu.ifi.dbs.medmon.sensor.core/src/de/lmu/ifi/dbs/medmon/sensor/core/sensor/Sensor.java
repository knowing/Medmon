package de.lmu.ifi.dbs.medmon.sensor.core.sensor;

public abstract class Sensor implements ISensor {

	private final String name;
	private final String version;
	private final int type;
	
	public Sensor(String name, String version, int type) {
		this.name = name;
		this.version = version;
		this.type = type;
	}
	
	public Sensor(String name, String version) {
		this.name = name;
		this.version = version;
		this.type = MASTER;
	}

	@Override
	public String getId() {
		return getName() + ":" + getVersion();
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getVersion() {
		return version;
	}
	
	@Override
	public int getType() {
		return type;
	}
}
