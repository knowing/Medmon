package de.lmu.ifi.dbs.medmon.sensor.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import de.lmu.ifi.dbs.medmon.sensor.core.device.IDeviceService;

public class DeviceServiceTest {

	private IDeviceService deviceService;

	@Before
	public void setUp() throws Exception {
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
		ServiceReference<IDeviceService> reference = context.getServiceReference(IDeviceService.class);
		deviceService = context.getService(reference);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testServiceAvailable() {
		assertNotNull(deviceService);
	}

}
