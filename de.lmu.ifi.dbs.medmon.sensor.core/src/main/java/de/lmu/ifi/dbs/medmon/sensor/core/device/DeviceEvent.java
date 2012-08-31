package de.lmu.ifi.dbs.medmon.sensor.core.device;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public class DeviceEvent {

	private final WatchEvent<Path>	event;
	private final Path				root;

	public DeviceEvent(WatchEvent<Path> event, Path root) {
		this.event = event;
		this.root = root;
	}

	public Path getPath() {
		return root.resolve(event.context());
	}

	public String getDeviceName() {
		return event.context().getFileName().toString();
	}

}
