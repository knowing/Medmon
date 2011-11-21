package de.lmu.ifi.dbs.medmon.medic.core.service;

import java.util.List;

import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

public interface ISensorObserver {
	
	/**
	 * called by the SensorService.
	 * passes the currently registered ISensor services.
	 * @param sensors
	 */
	public void init(List<ISensor> sensors);
	
	/**
	 * called by tje SensorService when an ISensor service gets registered
	 * @param service
	 */
	public void sensorAdded(ISensor service);
	
	/**
	 * called by tje SensorService when an ISensor service gets unregistered
	 * @param service
	 */
	public void sensorRemoved(ISensor service);
	
	/**
	 * called by tje SensorService when an ISensor service gets updated
	 * @param service
	 */
	public void sensorUpdated(ISensor service);
}
