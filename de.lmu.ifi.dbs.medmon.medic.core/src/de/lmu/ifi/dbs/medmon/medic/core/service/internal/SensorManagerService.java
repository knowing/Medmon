package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.medic.core.service.IEntityManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.service.ISensorManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.service.ISensorObserver;
import de.lmu.ifi.dbs.medmon.sensor.core.IConverter;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

public class SensorManagerService implements ISensorManagerService {

	private static Logger			log						= LoggerFactory.getLogger(SensorManagerService.class);
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
		@SuppressWarnings("unchecked")
		List<Sensor> results = entityManager.createNamedQuery("Sensor.findBySensorId").setParameter("sensorId", sensor.getId())
				.getResultList();

		if (results.isEmpty())
			return null;

		return results.get(0);
	}

	@Override
	public ISensor loadSensorService(Sensor sensor) {
		return sensorMap.get(sensor.getSensorId());
	}

	@Override
	public IConverter createConverter(ISensor sensorService) {

		if (sensorService == null)
			return null;

		InputStream inputStream = createInput(sensorService, 0);

		if (inputStream == null)
			return null;

		try {
			return sensorService.newConverter(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public URI[] availableInputs(ISensor sensor) {
		Sensor sensorEntity = loadSensorEntity(sensor);
		Path defaultPath = Paths.get(sensorEntity.getDefaultpath());
		List<URI> uriList = new ArrayList<URI>();

		try (DirectoryStream<Path> directoyStream = Files.newDirectoryStream(defaultPath)) {
			for (Path file : directoyStream) {
				if (file.toString().endsWith(sensor.getFilePrefix()))
					uriList.add(file.toUri());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (URI[]) uriList.toArray(new URI[uriList.size()]);
	}

	@Override
	public InputStream createInput(ISensor sensor, int inputIndex) {
		URI[] availableURL = availableInputs(sensor);

		if (availableURL.length == 0)
			return null;

		InputStream inputStream;

		try (InputStream newInputStream = new FileInputStream(new File(availableURL[inputIndex]))) {
			inputStream = newInputStream;
		} catch (Exception e) {
			log.error("createInput() -> couldn't create InputStream");
			e.printStackTrace();
			return null;
		}

		try {
			if (sensor.isConvertable(inputStream))
				return null;
		} catch (Exception e) {
			log.error("createInput() -> InputStream not convertable");
			e.printStackTrace();
		}

		return inputStream;
	}

	@Override
	public InputStream createInputs(ISensor sensor) {
		return createInput(sensor, 0);
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
