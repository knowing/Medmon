package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.medic.core.service.IEntityManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.service.ISensorObserver;
import de.lmu.ifi.dbs.medmon.medic.core.service.ISensorService;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.sensor.core.IConverter;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

public class SensorService implements ISensorService {

	private List<ISensor>			sensors					= new LinkedList<ISensor>();
	private List<ISensorObserver>	observers				= new LinkedList<ISensorObserver>();
	private IEntityManagerService	entityManagerService	= null;

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

		// something like
		// switch(type)
		// ...
		// return new FooSensor();

		return null;
	}

	@Override
	public IConverter createConverter(ISensor sSensor) {

		EntityManager entityManager = JPAUtil.createEntityManager();
		Query allSensors = entityManager.createNamedQuery("Sensor.findAll");
		List<Sensor> resultList = allSensors.getResultList();
		Sensor mSensor = null;

		for (Sensor sensor : resultList) {
			if (sensor.getId().equals(sSensor.getId()))
				mSensor = sensor;
		}

		if (mSensor == null)
			return null;

		Path defaultPath = Paths.get(mSensor.getDefaultpath());
		Path fittingFile = null;
		try {
			for (Path file : Files.newDirectoryStream(defaultPath)) {
				if (file.toString().endsWith(mSensor.getFilePrefix())) {
					fittingFile = file;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (fittingFile == null)
			return null;

		InputStream inputStream;

		try {
			inputStream = new FileInputStream(fittingFile.toFile());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		if (sSensor.isConvertable(inputStream))
			return null;

		return sSensor.newConverter(inputStream);
	}

	@Override
	public IConverter createConverter(Sensor sensor) {
		EntityManager entityManager = JPAUtil.createEntityManager();

		entityManager.getTransaction().begin();
		Sensor mSensor = entityManager.merge(sensor);
		entityManager.getTransaction().commit();

		Path defaultPath = Paths.get(mSensor.getDefaultpath());
		Path fittingFile = null;
		try {
			for (Path file : Files.newDirectoryStream(defaultPath)) {
				if (file.toString().endsWith(sensor.getFilePrefix())) {
					fittingFile = file;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (fittingFile == null)
			return null;

		ISensor sSensor = loadSensorEntity(mSensor);
		InputStream inputStream;

		try {
			inputStream = new FileInputStream(fittingFile.toFile());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		if (sSensor.isConvertable(inputStream))
			return null;

		return sSensor.newConverter(inputStream);
	}

	/**
	 * Service bindings and unbindings
	 */
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
		service.init(sensors);
	}

	protected void unbindSensorProvider(ISensorObserver service) {

	}

	protected void bindEntityManagerService(IEntityManagerService service) {
		entityManagerService = service;
	}

	protected void unbindEntityManagerService(IEntityManagerService service) {
		entityManagerService = null;
	}
}
