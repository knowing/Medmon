package de.lmu.ifi.dbs.medmon.medic.core.sensor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.osgi.service.component.ComponentContext;

import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.medic.core.service.IDerbyService;
import de.lmu.ifi.dbs.medmon.medic.core.service.ISensorService;
import de.lmu.ifi.dbs.medmon.sensor.core.sensor.ISensor;

public class SensorService implements ISensorService {

	private static final long INTERVAL = 1000;

	private PropertyChangeSupport support;
	private Map<String, SensorAdapter> model;
	private Thread currentThread;

	private IDerbyService dbService;

	protected void activate(ComponentContext context) {
		System.out.println("SensorServiceComponent activated");
		support = new PropertyChangeSupport(this);
		initModel();

		currentThread = new Thread(new SensorDaemon());
		currentThread.setDaemon(true);
		currentThread.setName("Sensor Daemon");
		currentThread.setPriority(Thread.MIN_PRIORITY);
		currentThread.start();
	}

	protected void deactivate(ComponentContext context) {
		currentThread.interrupt();
		currentThread = null;
	}

	@Override
	public Map<String, SensorAdapter> getSensorAdapters() {
		return model;
	}

	private void initModel() {
		model = Collections.synchronizedMap(new HashMap<String, SensorAdapter>());

		// Assert that the database is synchronized with extension points
		List<Sensor> entities = getSensorEntities();
		for (Sensor sensor : entities) {
			model.put(sensor.getId(), new SensorAdapter(sensor));
		}

		List<ISensor> extensions = getSensorExtensions();
		for (ISensor sensor : extensions) {
			String key = Sensor.parseId(sensor.getName(), sensor.getVersion());
			SensorAdapter adapter = model.get(key);
			adapter.setSensorExtension(sensor);
		}
		fireModelChanged();
	}

	private List<ISensor> getSensorExtensions() {
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(ISensor.SENSOR_ID);
		final LinkedList<ISensor> extensions = new LinkedList<ISensor>();
		try {
			for (IConfigurationElement e : config) {
				final Object o = e.createExecutableExtension("class");
				if (!(o instanceof ISensor))
					continue;

				ISafeRunnable runnable = new ISafeRunnable() {
					@Override
					public void handleException(Throwable exception) {
						exception.printStackTrace();
					}

					@Override
					public void run() throws Exception {
						extensions.add((ISensor) o);
					}
				};
				SafeRunner.run(runnable);

			}
		} catch (CoreException ex) {
			ex.printStackTrace();
		}
		return extensions;
	}

	private List<Sensor> getSensorEntities() {
		EntityManager em = dbService.createEntityManager();
		List<Sensor> resultList = em.createNamedQuery("Sensor.findAll", Sensor.class).getResultList();
		em.close();
		return resultList;
	}

	private void checkSensorsAvailable() {
		boolean changed = false;
		for (SensorAdapter adapter : model.values()) {
			// TODO search the db for new sensors
			// TODO search the extension points for new sensors
			boolean available = adapter.isAvailable();
			Sensor entity = adapter.getSensorEntity();
			if (entity == null && available) {
				changed = true;
				adapter.setAvailable(false);
				continue;
			} else {
				String path = entity.getDefaultpath();
				if (path == null || path.isEmpty()) {
					if (available) {
						changed = true;
						adapter.setAvailable(false);
						continue;
					}
				} else {
					File dir = new File(path);
					if (!dir.exists() && available) {
						changed = true;
						adapter.setAvailable(false);
						continue;
					}
					ISensor sensor = adapter.getSensorExtension();
					// Sensor extension not available
					if (sensor == null) {
						adapter.setAvailable(false);
						changed = true;
						continue;
					}
					// Sensor not null
					if (sensor.isSensor(dir)) {
						if (!available) {
							adapter.setAvailable(true);
							changed = true;
							continue;
						}
					} else {
						if (available) {
							adapter.setAvailable(false);
							changed = true;
							continue;
						}
					}
				}
			}
		}
		if (changed)
			fireModelChanged();

	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	protected void fireModelChanged() {
		if (support != null)
			support.firePropertyChange("model", null, model);
	}

	public void bindDerbyService(IDerbyService dbService) {
		this.dbService = dbService;
	}

	public void unbindDerbyService(IDerbyService dbService) {
		this.dbService = null;
	}

	private class SensorDaemon implements Runnable {

		@Override
		public void run() {
			try {
				while (!currentThread.isInterrupted()) {
					checkSensorsAvailable();
					Thread.sleep(INTERVAL);
				}
			} catch (InterruptedException e) {
				return;
			}
		}

	}

}
