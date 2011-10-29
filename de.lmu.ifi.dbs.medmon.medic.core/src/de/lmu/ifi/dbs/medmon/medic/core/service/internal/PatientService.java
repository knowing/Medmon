package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionService;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;

public class PatientService implements IPatientService {

	private final Logger log = LoggerFactory.getLogger(IGlobalSelectionService.class);
	
	protected void activate(ComponentContext context) {
		log.info("PatientService started successfully");
	}

	@Override
	public String locateDirectory(Patient p, String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String locateFile(Data d) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String locateFilename(Data d, String relativeToType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputStream store(Patient p, Sensor s, String type, Data from, Data to) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream load(Data d) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputStream merge(Data d1, Data d2) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Data d) {
		// TODO Auto-generated method stub
		return false;
	}
	

}
