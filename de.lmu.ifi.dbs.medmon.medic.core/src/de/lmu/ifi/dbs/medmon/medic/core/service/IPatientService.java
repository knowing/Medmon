package de.lmu.ifi.dbs.medmon.medic.core.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Sensor;

public interface IPatientService {

	public static String	TRAIN	= "train";
	public static String	RESULT	= "result";
	public static String	RAW		= "raw";
	public static String	ROOT	= "root";
	
	public String locateDirectory(Patient p, String type);
	public String locateFile(Data d);
	public String locateFilename(Data d, String relativeToType);
	
	public OutputStream store(Patient p, Sensor s, String type, Data from, Data to) throws IOException;
	public InputStream load(Data d) throws IOException;
	public OutputStream merge(Data d1, Data d2) throws IOException;
	public boolean remove(Data d);

}
