package de.lmu.ifi.dbs.medmon.sensor.core.device;

public interface IDeviceListener {

	public void deviceInserted(DeviceEvent event);
	
	public void deviceRemoved(DeviceEvent event);
}
