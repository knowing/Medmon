package de.lmu.ifi.dbs.medmon.sensor.test.sensors;

import de.lmu.ifi.dbs.medmon.sensor.core.AbstractSensor;
import de.lmu.ifi.dbs.medmon.sensor.core.Category;

public class SensorA extends AbstractSensor {

	@Override
	public String getName() {
		return "SensorA";
	}

	@Override
	public String getDescription() {
		return "Test Sensor A";
	}

	@Override
	public String getSerial() {
		return "aa:bb:cc:dd";
	}

	@Override
	public Category getCategory() {
		return Category.AC;
	}

}
