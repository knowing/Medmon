package de.lmu.ifi.dbs.medmon.database.install;

import static org.junit.Assert.*;

import javax.persistence.EntityManager;

import org.junit.Test;

import de.lmu.ifi.dbs.medmon.database.entity.Patient;

public class PatientTest extends DatabaseTest {


	@Test
	public void testPatient() {
		// Insert
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Patient patient = new Patient("John", "Smith", "ABC#" + Math.random());
		patient.setInsuranceId("abc123");
		
		assertTrue(patient.getId() == 0);
		em.persist(patient);
		assertTrue(patient.getId() != 0);
		
		em.getTransaction().commit();
		em.close();
		

		// Test if exists and remove
		em = emf.createEntityManager();
		Patient patientLoaded = em.find(Patient.class, patient.getId());
		assertNotNull(patientLoaded);
		
		em.getTransaction().begin();
		em.remove(patientLoaded);
		em.getTransaction().commit();
		em.close();
		
		//Test if patient got removed
		em = emf.createEntityManager();
		assertNull(em.find(Patient.class, patient.getId()));
		em.close();
		
	}

}
