package de.lmu.ifi.dbs.medmon.medic.core.service;

import java.beans.PropertyChangeListener;
import java.util.Map;

public interface ISensorService {

	Map<String, Object> getSensorAdapters();
	
	void addPropertyChangeListener(PropertyChangeListener listener);
	
	void removePropertyChangeListener(PropertyChangeListener listener);
}
