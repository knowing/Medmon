package de.lmu.ifi.dbs.medmon.sensor.test;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.device.Constants;
import org.osgi.service.device.Device;
import org.osgi.service.device.Driver;

import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensorDriver;
import de.lmu.ifi.dbs.medmon.sensor.test.sensors.DriverA;
import de.lmu.ifi.dbs.medmon.sensor.test.sensors.SensorA;

public class Activator implements BundleActivator {

	private static BundleContext context;

	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext ctx) throws Exception {
		Activator.context = ctx;
		//Driver (must be first)
		DriverA driverA = new DriverA();
		ctx.registerService(new String[] {ISensorDriver.class.getName(), Driver.class.getName()}, driverA, null);
		
		
		// Sensor
		SensorA sensorA = new SensorA();
		Dictionary<String, Object> sensorAProps = new Hashtable<>();
		sensorAProps.put(Constants.DEVICE_CATEGORY, sensorA.getCategory());
		sensorAProps.put(Constants.DEVICE_DESCRIPTION, sensorA.getDescription());
		ctx.registerService(new String[] {ISensor.class.getName(), Device.class.getName() }, sensorA, sensorAProps);
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext ctx) throws Exception {
		Activator.context = null;
	}

}
