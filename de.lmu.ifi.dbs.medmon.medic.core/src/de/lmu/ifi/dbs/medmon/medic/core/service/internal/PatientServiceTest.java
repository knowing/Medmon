package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import java.io.IOException;

import javax.persistence.EntityManager;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.service.IEntityManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;


public class PatientServiceTest {

	private final Logger log = LoggerFactory.getLogger(PatientServiceTest.class);
	
	private IPatientService patientService;
	private IEntityManagerService ems;
	
	protected void activate(ComponentContext context) {
		log.info("GlobalSelectionService started successfully");
		
/*		try {
			Patient p1 = patientService.createPatient();
			log.info(patientService.locateDirectory(p1, IPatientService.ROOT).toString());
			
			EntityManager em = ems.createEntityManager();
			em.getTransaction().begin();
			Patient patient = em.merge(p1);
			patient.setFirstname("John");
			patient.setLastname("Smith");
			em.getTransaction().commit();
			em.close();
			
			Patient p2 = patientService.createPatient();
			log.info(patientService.locateDirectory(p2, IPatientService.ROOT).toString());
			
			patientService.deletePatient(p2);
			
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
	}
	
	protected void bindPatientService(IPatientService patientService) {
		this.patientService = patientService;
	}
	
	protected void unbindPatientService(IPatientService patientService) {
		this.patientService = null;
	}
	
	protected void bindEntityManagerService(IEntityManagerService ems) {
		this.ems = ems;
	}
	
	protected void unbindEntityManagerService(IEntityManagerService ems) {
		this.ems = null;
	}
	
}
