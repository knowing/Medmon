package de.lmu.ifi.dbs.medmon.sensor.core.interal;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.lmu.ifi.dbs.medmon.sensor.core.device.DeviceEvent;
import de.lmu.ifi.dbs.medmon.sensor.core.device.IDeviceListener;
import de.lmu.ifi.dbs.medmon.sensor.core.internal.DeviceService;

public class DeviceServiceTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testActivate() {
		DeviceService service = new DeviceService();
		service.activate(Collections.<String, Object> emptyMap());
	}

	@Test
	public void testDeactivate() {
		DeviceService service = new DeviceService();
		service.activate(Collections.<String, Object> emptyMap());
		service.deactivate();
	}

	@Test
	public void testAddListenerBeforeActivation() throws Exception {
		DeviceService service = new DeviceService();
		
		service.addListener(new IDeviceListener() {
			
			@Override
			public void deviceRemoved(DeviceEvent event) {
				
			}
			
			@Override
			public void deviceInserted(DeviceEvent event) {
				System.err.println("DeviceName " + event.getDeviceName());
				System.err.println("Path: " + event.getPath());
			} 
		});
		service.activate(Collections.<String, Object> emptyMap());
		service.deactivate();
	}
	
	@Test
	public void testAddListenerAfterActivation() {
		DeviceService service = new DeviceService();
		service.activate(Collections.<String, Object> emptyMap());
		
		service.addListener(new IDeviceListener() {
			
			@Override
			public void deviceRemoved(DeviceEvent event) {
				
			}
			
			@Override
			public void deviceInserted(DeviceEvent event) {
				System.err.println("DeviceName " + event.getDeviceName());
				System.err.println("Path: " + event.getPath());
			} 
		});

	}


	@Test
	public void testRemoveListener() {
		fail("Not yet implemented");
	}

}
