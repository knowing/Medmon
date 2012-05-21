package de.lmu.ifi.dbs.medmon.sensor.core.internal;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.sensor.core.device.DeviceEvent;
import de.lmu.ifi.dbs.medmon.sensor.core.device.IDeviceListener;
import de.lmu.ifi.dbs.medmon.sensor.core.device.IDeviceService;

/**
 * Watches for changes in the common mount directories for
 * removable devices like USB, SD cards and external harddisks.
 * 
 * 
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 2012-05-21
 */
public class DeviceService implements IDeviceService {

	private final Logger			log			= LoggerFactory.getLogger(IDeviceService.class);
	private Map<String, Object>		properties;
	private Watcher					watcher;

	private Set<IDeviceListener>	listeners	= new HashSet<>();

	@Override
	public void activate(Map<String, Object> properties) {
		this.properties = properties;
		String os = System.getProperty("os.name").toLowerCase();
		try {
			if (os.contains("linux")) {
				watchLinux();
			} else if (os.contains("win")) {
				watchWindows();
			}
		} catch (IOException e) {
			log.error("Failed to start device service!", e);
		}

	}

	protected void watchLinux() throws IOException {
		// /dev/disk/by-path/ ---> dev/sd abcdef
		// /media/
		Path media = Paths.get("/", "media");
		if (!Files.exists(media)) {
			log.error("No mount folder 'media' found!");
			return;
		}
		watcher = new Watcher(media);
		watcher.start();
	}

	protected void watchWindows() {
		Iterable<Path> roots = FileSystems.getDefault().getRootDirectories();
	}

	@Override
	public void deactivate() {
		watcher.interrupt();
		watcher = null;
		listeners.clear();
	}

	/**
	 * React on changes in the configuration
	 * 
	 * @param properties
	 */
	public void modified(Map<String, Object> properties) {
		this.properties = properties;
	}

	@Override
	public void addListener(IDeviceListener listener) {
		log.debug("Add listener " + listener);
		listeners.add(listener);
	}

	@Override
	public void removeListener(IDeviceListener listener) {
		log.debug("Remove listener " + listener);
		listeners.remove(listener);
	}

	/**
	 * Thread to watch particular directory
	 * 
	 * @author Nepomuk Seiler
	 * 
	 */
	private class Watcher extends Thread {

		private final WatchService	watchService;
		private final Path			root;

		public Watcher(Path root) throws IOException {
			this.root = root;
			watchService = FileSystems.getDefault().newWatchService();
			root.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
		}

		@Override
		public void run() {
			log.info("Watching device path " + root);
			while (true) {
				// wait for key to be signaled
				WatchKey key;
				try {
					log.debug("waiting for event");
					key = watchService.take();
				} catch (InterruptedException e) {
					log.warn("Device Watcher got interrupted.");
					return;
				}

				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path filename = ev.context();
					log.debug("Device changed " + filename);
					DeviceEvent deviceEvent = new DeviceEvent(ev, root);
					if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
						for (IDeviceListener listener : listeners)
							listener.deviceInserted(deviceEvent);
					} else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
						for (IDeviceListener listener : listeners)
							listener.deviceRemoved(deviceEvent);
					}

				}
				if (!key.reset()) {
					return;
				}
			}
		}
	}

}
