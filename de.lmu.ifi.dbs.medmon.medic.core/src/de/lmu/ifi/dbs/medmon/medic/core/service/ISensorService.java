package de.lmu.ifi.dbs.medmon.medic.core.service;

import java.beans.PropertyChangeListener;
import java.util.Map;

import de.lmu.ifi.dbs.medmon.medic.core.sensor.SensorAdapter;

public interface ISensorService {

	Map<String, SensorAdapter> getSensorAdapters();
	
	void addPropertyChangeListener(PropertyChangeListener listener);
	
	void removePropertyChangeListener(PropertyChangeListener listener);
}
