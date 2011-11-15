package de.lmu.ifi.dbs.medmon.sensor.core.sensor;

import java.io.InputStream;

import de.lmu.ifi.dbs.medmon.sensor.core.converter.IConverter;


public interface ISensor {

	public String getId();
	
	public String getName();
	
	public String getDescription();
	
	public String getVersion();
	
	public String getFilePrefix();
	
	public boolean isConvertable(InputStream inputStream);
	
	public IConverter newConverter(InputStream inputStream);
	
	
}
