package de.lmu.ifi.dbs.medmon.sensor.core.device;

import java.util.Map;

/**
 * Manages standard mass data storage devices. Sends events
 * to all listeners if a new device is registered or an
 * existing removed.
 * 
 * @author Nepomuk Seiler
 *
 */
public interface IDeviceService {

	public void addListener(IDeviceListener listener);
	
	public void removeListener(IDeviceListener listener);
	
	public void activate(Map<String, Object> properties);
	
	public void deactivate();
}
