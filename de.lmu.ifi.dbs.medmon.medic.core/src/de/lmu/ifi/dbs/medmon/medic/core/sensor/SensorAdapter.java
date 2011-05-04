package de.lmu.ifi.dbs.medmon.medic.core.sensor;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.sensor.core.container.IBlock;
import de.lmu.ifi.dbs.medmon.sensor.core.converter.IConverter;
import de.lmu.ifi.dbs.medmon.sensor.core.sensor.ISensor;

public class SensorAdapter {

	private String name;
	private String version;
	private String type;

	private boolean available;
	private String tooltip;
	
	private ISensor sensorExtension;
	private Sensor sensorEntity;
	
	public SensorAdapter(ISensor sensorExtension, Sensor sensorEntity) {
		this.sensorExtension = sensorExtension;
		this.sensorEntity = sensorEntity;
		this.name = sensorExtension.getName();
		this.version = sensorExtension.getVersion();
		this.type = type(sensorExtension.getType());
		
		this.available = true;
	}
	
	public SensorAdapter(ISensor sensorExtension) {
		this.sensorExtension = sensorExtension;
		this.name = sensorExtension.getName();
		this.version = sensorExtension.getVersion();
		this.type = type(sensorExtension.getType());
		
		this.available = false;
	}
	
	public SensorAdapter(Sensor sensorEntity) {
		this.sensorEntity = sensorEntity;
		this.name = sensorEntity.getName();
		this.version = sensorEntity.getVersion();
		this.type = type(sensorEntity.getType());
		
		this.available = false;
	}
	
	public void copy(OutputStream out) throws IOException {
		if(sensorEntity == null || sensorExtension == null)
			throw new IOException("Unable to find sensor in database( "+sensorEntity+" )or extension-registry( "+sensorExtension+" )");
		IConverter converter = sensorExtension.getConverter();
		converter.setDirectory(sensorEntity.getDefaultpath());
		converter.copy(out);
	}
	
	public IBlock convert() throws IOException {
		if(sensorEntity == null || sensorExtension == null)
			throw new IOException("Unable to find sensor in database( "+sensorEntity+" )or extension-registry( "+sensorExtension+" )");
		IConverter converter = sensorExtension.getConverter();
		converter.setDirectory(sensorEntity.getDefaultpath());
		return converter.convert();
	}

	public boolean isAvailable() {
		return available;
	}
	
	public void setAvailable(boolean available) {
		this.available = available;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getType() {
		return type;
	}

	public String getDefaultPath() {
		return sensorEntity.getDefaultpath();
	}
	
	public void setDefaultPath(String defaultpath) {
		sensorEntity.setDefaultpath(defaultpath);
	}
	
	public ISensor getSensorExtension() {
		return sensorExtension;
	}
	
	protected void setSensorExtension(ISensor sensorExtenion) {
		this.sensorExtension = sensorExtenion;
	}

	public Sensor getSensorEntity() {
		return sensorEntity;
	}
	
	protected void setSensorEntity(Sensor sensorEntity) {
		this.sensorEntity = sensorEntity;
	}
	
	private String type(int type) {
		switch(type) {
		case ISensor.MASTER: return "Master";
		case ISensor.SLAVE: return "Slave";
		default: return "unbekannt";
		}	
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SensorAdapter [name=");
		builder.append(name);
		builder.append(", version=");
		builder.append(version);
		builder.append(", type=");
		builder.append(type);
		builder.append(", defaultPath=");
		builder.append(sensorEntity.getDefaultpath());
		builder.append(", available=");
		builder.append(available);
		builder.append(", sensorExtension=");
		builder.append(sensorExtension);
		builder.append(", sensorEntity=");
		builder.append(sensorEntity);
		builder.append("]");
		return builder.toString();
	}
	
}
