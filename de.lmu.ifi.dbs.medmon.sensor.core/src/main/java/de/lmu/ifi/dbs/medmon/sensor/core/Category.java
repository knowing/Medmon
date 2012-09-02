package de.lmu.ifi.dbs.medmon.sensor.core;

public enum Category {

	AC,
	BC;
	
	private Category() {
	}
	
	public String fullName() {
		return "sensor." + name();
	}
	
}
