package de.lmu.ifi.dbs.medmon.sensor.core.device;

import java.util.Map;

public interface IDeviceService {

	public void addListener(IDeviceListener listener);
	
	public void removeListener(IDeviceListener listener);
	
	public void activate(Map<String, Object> properties);
	
	public void deactivate();
}
