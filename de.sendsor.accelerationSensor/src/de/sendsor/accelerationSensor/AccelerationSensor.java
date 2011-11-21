package de.sendsor.accelerationSensor;

import java.io.IOException;
import java.io.InputStream;

import de.lmu.ifi.dbs.medmon.sensor.core.IConverter;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

/**
 * 
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 2011-11-21
 *
 */
public class AccelerationSensor implements ISensor {

	@Override
	public String getId() {
		return getClass().getName() + ":" + getVersion();
	}

	@Override
	public String getName() {
		return "Sendsor Acceleration Sensor";
	}

	@Override
	public String getDescription() {
		return "3D Acceleration Senesor";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getFilePrefix() {
		return "sdr";
	}

	@Override
	public boolean isConvertable(InputStream inputStream) throws IOException {
		return true;
	}

	@Override
	public IConverter newConverter(InputStream inputStream) throws IOException {
		return new SDRConverter(inputStream);
	}

}
