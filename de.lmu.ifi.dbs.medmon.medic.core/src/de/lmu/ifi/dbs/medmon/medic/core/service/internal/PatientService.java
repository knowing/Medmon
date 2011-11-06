package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Date;

import javax.persistence.EntityManager;

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

	private final Logger log = LoggerFactory.getLogger(IGlobalSelectionService.class);
	private IEntityManagerService entityManagerService;
	FileSystem fileSystem = FileSystems.getDefault();

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
		return fileSystem.getPath(String.valueOf(p.getId()), type);
	}

	@Override
	public Path locateFile(Data d) {
		return fileSystem.getPath(String.valueOf(d.getPatient().getId()), d.getType(), d.getFile());
	}

	@Override
	public Path locateFilename(Data d, String relativeToType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputStream store(Patient p, Sensor s, String type, Date from, Date to) throws IOException {

		return null;
	}

	@Override
	public InputStream load(Data d) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream merge(Data d1, Data d2) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Data d) {
		// TODO Auto-generated method stub
		return false;
	}

}
