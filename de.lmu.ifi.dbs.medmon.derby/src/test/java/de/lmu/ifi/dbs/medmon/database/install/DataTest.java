package de.lmu.ifi.dbs.medmon.database.install;

import static org.junit.Assert.*;

import java.util.Date;

import javax.persistence.EntityManager;

import org.junit.Test;

import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Sensor;

public class DataTest extends DatabaseTest {

	@Test
	public void testPlainData() {
		Patient patient = createPatient();
		Sensor sensor = createSensor();
		
		//Create Data
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Data data = new Data(patient, sensor, "raw", new Date(0), new Date(10));
		
		assertEquals("Id should not be set", data.getId(), 0);
		em.persist(data);
		assertTrue("Id should be set", data.getId() != 0);
		em.getTransaction().commit();
		em.close();
		
		//Remove Data
		em = emf.createEntityManager();
		em.getTransaction().begin();
		Data d = em.find(Data.class, data.getId());
		assertNotNull("Data not persisted!",d);
		assertNotNull("Relation to Patient not persisted",d.getPatient());
		assertNotNull("Relation to Sensor not persisted",d.getSensor());
		
		assertEquals("Patients not equally", d.getPatient(), patient);
		assertEquals("Sensors not equally", d.getSensor(), sensor);
		
		em.remove(d);
		em.getTransaction().commit();
		em.close();
		
		removePatient(patient);
		removeSensor(sensor);
	}

}
