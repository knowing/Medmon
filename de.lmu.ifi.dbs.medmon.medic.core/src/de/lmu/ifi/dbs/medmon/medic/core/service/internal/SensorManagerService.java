package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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

import de.lmu.ifi.dbs.medmon.database.entity.Sensor;
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
	public void notifySensorObservers(ISensor sensor) {
		for (ISensorObserver observer : observers) {
			observer.sensorUpdated(sensor);
		}		
	}

	@Override
	@SuppressWarnings("unchecked")
	public Sensor loadSensorEntity(ISensor sensor) {
		EntityManager tempEM = entityManagerService.createEntityManager();
		List<Sensor> results = tempEM.createNamedQuery("Sensor.findBySensorId")
				.setParameter("sensorId", sensor.getId())
				.getResultList();

		tempEM.close();
		if (results.isEmpty())
			return null;
		
		return results.get(0);
	}

	@Override
	public ISensor loadSensorService(Sensor sensor) {
		return sensorMap.get(sensor.getSensorId());
	}

	@Override
	public IConverter createConverter(ISensor sensorService) throws IOException {

		if (sensorService == null)
			return null;

		InputStream inputStream = createDefaultInput(sensorService);

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
	public InputStream createDefaultInput(ISensor sensor) throws IOException {
		return createInput(sensor, availableInputs(sensor)[0]);
	}

	@Override
	public InputStream createInput(ISensor sensor, URI uri) throws IOException {
		try (InputStream newInputStream = new FileInputStream(new File(uri))) {
			if (!sensor.isConvertable(newInputStream))
				return null;
		} catch (Exception e) {
			log.error("createInput() -> InputStream not convertable");
			e.printStackTrace();
		}
		return Files.newInputStream(Paths.get(uri));
	}

	@Override
	public InputStream[] createInputs(ISensor sensor) throws IOException {
		URI[] availableURIs = availableInputs(sensor);
		InputStream[] inputStreams = new InputStream[availableURIs.length];
		for (int index = 0; index < availableURIs.length; index++) {
			inputStreams[index] = createInput(sensor, availableURIs[index]);
		}
		return inputStreams;
	}

	/**
	 * Service bindings and unbindings
	 */
	protected void bindSensor(ISensor service) {
		sensorList.add(service);
		sensorMap.put(service.getId(), service);

		if (loadSensorEntity(service) == null) {
			EntityManager tempEM = entityManagerService.createEntityManager();

			Sensor mSensor = new Sensor(service.getName(), service.getId(), service.getVersion());
			mSensor.setDefaultpath(System.getProperty("user.home"));
			mSensor.setFilePrefix(service.getFilePrefix());

			tempEM.getTransaction().begin();
			tempEM.persist(mSensor);
			tempEM.getTransaction().commit();
			
			tempEM.close();

			log.info("Sensor " + service.getName() + " " + service.getVersion() + " registered and DB entry created");
		}

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
