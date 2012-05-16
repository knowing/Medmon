package de.lmu.ifi.dbs.medmon.sensor.core.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensorDirectoryService;

public class SensorDirectoryService implements ISensorDirectoryService {

	private Logger log = LoggerFactory.getLogger(ISensorDirectoryService.class);
	private Map<String, ISensor> sensors = new HashMap<>();
	private EventAdmin eventAdmin; 
	
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
		HashMap<String, Object> properties = new HashMap<>();
		properties.put(SENSOR_PROPERTY_ADD, sensor);
		properties.put("org.eclipse.e4.data", sensor); //This is for e4
		eventAdmin.postEvent(new Event(SENSOR_TOPIC_CHANGED, properties));
		log.debug("Added sensor "  + sensor.getId());
	}

	protected void unbindSensor(ISensor sensor) {
		sensors.remove(sensor.getId());
		HashMap<String, Object> properties = new HashMap<>();
		properties.put(SENSOR_PROPERTY_REMOVED, sensor);
		properties.put("org.eclipse.e4.data", sensor); //This is for e4
		eventAdmin.postEvent(new Event(SENSOR_TOPIC_CHANGED, properties));
		log.debug("Removed sensor "  + sensor.getId());
	}
	
	protected void bindEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
		log.debug("Bind event admin");
	}

	protected void unbindEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = null;
		log.debug("Unbind event admin");
	}
	
}
