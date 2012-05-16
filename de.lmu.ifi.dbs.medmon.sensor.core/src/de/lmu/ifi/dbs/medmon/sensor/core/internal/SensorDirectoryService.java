package de.lmu.ifi.dbs.medmon.sensor.core.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensorDirectoryService;

public class SensorDirectoryService implements ISensorDirectoryService {

	private Logger log = LoggerFactory.getLogger(ISensorDirectoryService.class);
	private Map<String, ISensor> sensors = new HashMap<>(); 
	
	@Override
	public List<ISensor> getSensors() {
		return Collections.unmodifiableList(new ArrayList<>(sensors.values()));
	}

	@Override
	public ISensor getSensor(String id) {
		return sensors.get(id);
	}
	
	protected void activate() {
		log.debug("SensorDirectoryService activated");
	}
	
	protected void bindSensor(ISensor sensor) {
		sensors.put(sensor.getId(), sensor);
		log.debug("Added sensor");
	}

	protected void unbindSensor(ISensor sensor) {
		sensors.remove(sensor);
	}

}
