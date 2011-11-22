package de.lmu.ifi.dbs.medmon.sensor.core;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractSensor implements ISensor {

	private String	id;

	@Override
	public String getId() {
		return getClass().toString() + getVersion();
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getVersion() {
		return null;
	}

	@Override
	public String getFilePrefix() {
		return null;
	}

	@Override
	public boolean isConvertable(InputStream inputStream) throws IOException {
		return false;
	}

	@Override
	public IConverter newConverter(InputStream inputStream) throws IOException {
		return null;
	}

}
