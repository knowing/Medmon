package de.sendsor.accelerationSensor.sensor;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import weka.core.Instances;
import de.lmu.ifi.dbs.medmon.sensor.core.IConverter;
import de.sendsor.accelerationSensor.converter.SDRConverter;

public class Sensor3D { //extends Sensor {

	/*
	private static final SDRConverter converter = new SDRConverter();
	
	public Sensor3D() {
		super("3D Master Sensor", "1.0");
	}

	@Override
	public Instances getData(String path) throws IOException {
		//Sensor exists
		File sensorpath = new File(path);
		if(!sensorpath.exists() || !sensorpath.isDirectory())
			throw new IOException("Wrong path");
		
		//Get .sdr files
		File[] sdrFiles = sensorpath.listFiles(new FilenameFilter() {			
			@Override
			public boolean accept(File dir, String name) {		
				return name.endsWith(".sdr");
			}
		});
		
		//Sensor contains data
		if(sdrFiles.length < 1)
			throw new IOException("Sensor contains no data");
		
		converter.setFile(sdrFiles[0]);
		
		return converter.getDataSet();
	}
	
	@Override
	public boolean isSensor(File dir) {
		String[] files = dir.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".sdr");
			}
		});
		return files.length > 0;
	}
	
	@Override
	public IConverter getConverter() {
		return converter;
	}

	@Override
	public String getDescription() {
		return "3D Bewegungssensor zum Aufzeichnen von Bewegungsdaten.";
	}
	*/
}
