package de.lmu.ifi.dbs.medmon.sensor.test;

import java.io.IOException;
import java.util.Properties;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class DeviceServiceConfigurationService {

	private ConfigurationAdmin	confAdmin;
	
	private void configureService() {
		try {
			Configuration conf = confAdmin.getConfiguration("de.lmu.ifi.dbs.medmon.sensor.core.device.IDeviceService");
			Properties properties = new Properties();
			properties.setProperty("paths", "8Gb");
			conf.update(properties);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void activate() {
		System.out.println("DeviceServiceConfigurationService.name()");
		configureService();
	}

	protected void bindConfigService(ConfigurationAdmin confAdmin) {
		this.confAdmin = confAdmin;
		System.out.println("DeviceServiceConfigurationService.bindConfigService()");
	}
	
	protected void unbindConfigService(ConfigurationAdmin confAdmin) {
		this.confAdmin = null;
		System.out.println("DeviceServiceConfigurationService.unbindConfigService()");		
	}

}
