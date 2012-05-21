package de.lmu.ifi.dbs.medmon.sensor.test;

import de.lmu.ifi.dbs.medmon.sensor.core.device.DeviceEvent;
import de.lmu.ifi.dbs.medmon.sensor.core.device.IDeviceListener;

public class DeviceListener implements IDeviceListener {

	protected void activate() {
		System.out.println("DeviceListener.activated()");
	}

	@Override
	public void deviceInserted(DeviceEvent event) {
		System.out.println("deviceInserted: " + event.getDeviceName());
		System.out.println(event.getPath());
	}

	@Override
	public void deviceRemoved(DeviceEvent event) {
		System.out.println("deviceRemoved: " + event.getDeviceName());
		System.out.println(event.getPath());
	}

}
