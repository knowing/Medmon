package de.lmu.ifi.dbs.medmon.medic.core.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Sensor;

public interface IPatientService {

	public static String	TRAIN	= "train";
	public static String	RESULT	= "result";
	public static String	RAW		= "raw";
	public static String	ROOT	= "root";
	
	public Path locateDirectory(Patient p, String type);
	public Path locateFile(Data d);
	public Path locateFilename(Data d, String relativeToType);
	
	public Path store(Patient p, Sensor s, String type, Data from, Data to);
	public Path load(Data d);
	public Path merge(Data d1, Data d2);
	public boolean remove(Data d);

}
