package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.medic.core.service.ISensorObserver;
import de.lmu.ifi.dbs.medmon.medic.core.service.ISensorService;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;
import de.lmu.ifi.dbs.medmon.sensor.core.converter.IConverter;

public class SensorService implements ISensorService {

	private List<ISensor>			sensors		= new LinkedList<ISensor>();
	private List<ISensorObserver>	observers	= new LinkedList<ISensorObserver>();

	@Override
	public ISensor[] getSensors() {
		return (ISensor[]) sensors.toArray();
	}

	@Override
	public ISensor getSensor(String id) {
		for (ISensor sensor : sensors) {
			if (sensor.getId().equals(id))
				return sensor;
		}
		return null;
	}

	@Override
	public ISensor loadSensorEntity(Sensor sensor) {

		return null;
	}

	@Override
	public IConverter createConverter(ISensor sensor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IConverter createConverter(Sensor sensor) {
		// TODO Auto-generated method stub
		return null;
	}

	protected void bindSensor(ISensor service) {
		sensors.add(service);
		for (ISensorObserver observer : observers) {
			observer.sensorAdded(service);
		}
	}

	protected void unbindSensor(ISensor service) {
		sensors.remove(service);
		for (ISensorObserver observer : observers) {
			observer.sensorRemoved(service);
		}
	}

	protected void bindSensorProvider(ISensorObserver service) {
		service.setSensorService(this);
		service.init(sensors);
	}

	protected void unbindSensorProvider(ISensorObserver service) {
		// nothing
	}
}
