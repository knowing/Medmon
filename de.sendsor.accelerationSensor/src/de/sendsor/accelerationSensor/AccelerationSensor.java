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

	private final String	id	= getClass().getName() + ":" + getVersion();

	@Override
	public String getId() {
		return id;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AccelerationSensor other = (AccelerationSensor) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	
}
