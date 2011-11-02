package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.medic.core.service.IEntityManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionService;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;

public class PatientService implements IPatientService {

	private final Logger			log	= LoggerFactory.getLogger(IGlobalSelectionService.class);
	private IEntityManagerService	entityManagerService;

	protected void activate(ComponentContext context) {
		log.info("PatientService started successfully");
	}

	protected void bindProvider(IEntityManagerService service) {
		entityManagerService = service;
	}

	protected void unbindProvider(IEntityManagerService service) {
		entityManagerService = null;
	}

	@Override
	public Path locateDirectory(Patient p, String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path locateFile(Data d) {
		return null;
	}

	@Override
	public Path locateFilename(Data d, String relativeToType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path store(Patient p, Sensor s, String type, Data from, Data to) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path load(Data d) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path merge(Data d1, Data d2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Data d) {
		// TODO Auto-generated method stub
		return false;
	}

}
