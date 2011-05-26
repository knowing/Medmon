package de.lmu.ifi.dbs.medmon.medic.core.sensor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;

import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.database.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.medic.core.Activator;
import de.lmu.ifi.dbs.medmon.sensor.core.sensor.ISensor;

/**
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 01.05.2011
 * 
 */
public class SensorDaemon implements Runnable {

	private static final Logger logger = Logger.getLogger(Activator.PLUGIN_ID);
	private static final long INTERVAL = 1000;

	private static SensorDaemon daemon;
	private static Thread currentThread;

	private volatile boolean initialized;
	private PropertyChangeSupport support;
	private Map<String, SensorAdapter> model;

	private int attempts = 0;

	private SensorDaemon() {
		support = new PropertyChangeSupport(this);
	}

	@Override
	public void run() {
		while (!currentThread.isInterrupted()) {
			if (!initialized && JPAUtil.isAvailable()) {
				// singleton = new SensorDaemonOld();
				synchronize();
				initModel();
				checkSensorsAvailable();
				fireModelChanged();
				initialized = true;
				logger.info("Daemon initialized");
			} else if (initialized) {
				checkSensorsAvailable();
				logger.finest("Check Sensors");
			}

			try {
				Thread.sleep(INTERVAL);
			} catch (InterruptedException e) {
				System.err.println("SensorDaemon interrupted -> terminate!");
				initialized = false;
				return;
			}
		}

	}

	public static SensorDaemon getDaemon() {
		return daemon;
	}

	public static void start() {
		if (daemon != null && currentThread.isAlive())
			return;

		daemon = new SensorDaemon();
		currentThread = new Thread(daemon);
		currentThread.setDaemon(true);
		currentThread.setName("Sensor Daemon");
		currentThread.setPriority(Thread.MIN_PRIORITY);
		currentThread.start();
	}

	public static void stop() {
		if (daemon != null && currentThread != null)
			currentThread.interrupt();
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
						logger.severe("Exception in client");
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
			logger.severe(ex.getMessage());
		}
		return extensions;
	}

	private List<Sensor> getSensorEntities() {
		EntityManager em = getEntityManager();
		List<Sensor> resultList = em.createNamedQuery("Sensor.findAll", Sensor.class).getResultList();
		em.close();
		return resultList;
	}

	private void synchronize() {
		List<ISensor> sensors = getSensorExtensions();
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		for (ISensor sensor : sensors) {
			String id = sensor.getName() + ":" + sensor.getVersion();
			Sensor dbsensor = em.find(Sensor.class, id);
			if (dbsensor == null) {
				em.persist(new Sensor(sensor.getName(), sensor.getVersion(), sensor.getType()));
			}
		}
		em.getTransaction().commit();
		em.close();
	}

	public void checkSensorsAvailable() {
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
					//Sensor extension not available
					if(sensor == null) {
						adapter.setAvailable(false);
						changed = true;
						continue;
					}
					//Sensor not null
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

	private EntityManager getEntityManager() {
		try {
			if (attempts < 10) {
				EntityManager em = JPAUtil.createEntityManager();
				attempts = -1;
				return em;
			}
		} catch (NullPointerException e) {
			System.err.println("Couldn't create EntityManager: JPAUTil not available");
			try {
				System.err.println("Waiting for " + (500 * (attempts+1)) + " milliseconds");
				Thread.sleep(500 * (attempts+1));
			} catch (InterruptedException e1) {
				System.err.println("Waiting for JPAUtil interrupted...");
				return null;
			}
			attempts++;
			if(attempts < 10)
				return getEntityManager();
		}
		return null;
	}

	public Map<String, SensorAdapter> getModel() {
		return model;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	protected void fireModelChanged() {
		if (support != null)
			support.firePropertyChange("model", null, model);
	}
}
