package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.osgi.service.component.ComponentContext;

import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.medic.core.service.IEntityManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.service.ISensorObserver;
import de.lmu.ifi.dbs.medmon.medic.core.service.ISensorService;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.sensor.core.IConverter;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

public class SensorService implements ISensorService {

	private Map<String, ISensor>	sensorMap				= new HashMap<String, ISensor>();
	private List<ISensor>			sensorList				= new ArrayList<ISensor>();
	private List<ISensorObserver>	observers				= new LinkedList<ISensorObserver>();
	private IEntityManagerService	entityManagerService	= null;

	@Override
	public ISensor[] getSensors() {
		return (ISensor[]) sensorList.toArray();
	}

	@Override
	public ISensor getSensor(String id) {
		return sensorMap.get(id);
	}

	@Override
	public Sensor loadSensorEntity(ISensor sensor) {
		
		EntityManager entityManager = entityManagerService.getEntityManager();
		List<Sensor> results = entityManager.createNamedQuery("Sensor.findBySensorId")
		.setParameter("sensorId", sensor.getId())
		.getResultList();

		if(results.isEmpty())
			return null;
		
		return results.get(0);
	}

	@Override
	public IConverter createConverter(ISensor sensorService) {
		
		Sensor sensor = loadSensorEntity(sensorService);
		return createConverter(sensor);
		
	}

	@Override
	public IConverter createConverter(Sensor sensor) {
		
		if (sensor == null)
			return null;

		Path defaultPath = Paths.get(sensor.getDefaultpath());
		Path fittingFile = null;
		try (DirectoryStream<Path> directoyStream = Files.newDirectoryStream(defaultPath)) {
			for (Path file : directoyStream) {
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

		InputStream inputStream;

		try(InputStream newInputStream = new FileInputStream(fittingFile.toFile())) {
			inputStream = newInputStream;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		ISensor sensorService = sensorMap.get(sensor.getSensorId());
		
		try {
			if (sensorService.isConvertable(inputStream))
				return null;

			return sensorService.newConverter(inputStream);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
		
		/*
		EntityManager entityManager = JPAUtil.createEntityManager();

		entityManager.getTransaction().begin();
		Sensor mSensor = entityManager.merge(sensor);
		entityManager.getTransaction().commit();

		ISensor sSensor = null; //loadSensorEntity(mSensor);
		return createConverter(sSensor);
		*/
	}
	
	/**
	 * Service bindings and unbindings
	 */
	protected void bindSensor(ISensor service) {
		sensorList.add(service);
		sensorMap.put(service.getId(), service);
		for (ISensorObserver observer : observers) {
			observer.sensorAdded(service);
		}
	}

	protected void unbindSensor(ISensor service) {
		sensorList.add(service);
		sensorMap.remove(service);
		for (ISensorObserver observer : observers) {
			observer.sensorRemoved(service);
		}
	}

	protected void bindSensorObserver(ISensorObserver service) {
		observers.add(service);
		service.init(sensorList);
	}

	protected void unbindSensorObserver(ISensorObserver service) {
		observers.remove(service);
	}

	protected void bindEntityManagerService(IEntityManagerService service) {
		entityManagerService = service;
	}

	protected void unbindEntityManagerService(IEntityManagerService service) {
		entityManagerService = null;
	}
}
