package de.lmu.ifi.dbs.medmon.sensor.test.sensors;

import java.io.IOException;
import java.io.InputStream;

import org.joda.time.Interval;
import org.osgi.framework.ServiceReference;

import weka.core.Instances;
import de.lmu.ifi.dbs.medmon.sensor.core.AbstractSensorDriver;
import de.lmu.ifi.dbs.medmon.sensor.core.Category;

public class DriverA extends AbstractSensorDriver {

	@Override
	public Instances getData(InputStream in) throws IOException {
		return null;
	}

	@Override
	public Instances getHeader() {
		return null;
	}

	@Override
	public String getFilePrefix() {
		return null;
	}

	@Override
	public Interval getInterval(InputStream in) throws IOException {
		return null;
	}

    @Override
    public int match(Category category, ServiceReference reference) {
        return 1;
    }


}
