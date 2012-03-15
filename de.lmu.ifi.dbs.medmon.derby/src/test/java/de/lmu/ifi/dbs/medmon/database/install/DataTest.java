package de.lmu.ifi.dbs.medmon.database.install;

import static org.junit.Assert.*;

import java.util.Date;

import javax.persistence.EntityManager;

import org.junit.Test;

import de.lmu.ifi.dbs.medmon.database.entity.Data;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.database.entity.Sensor;
import de.lmu.ifi.dbs.medmon.database.entity.Therapy;
import de.lmu.ifi.dbs.medmon.database.entity.TherapyResult;

public class DataTest extends DatabaseTest {

	@Test
	public void testPlainData() {
		Patient patient = createPatient();
		Sensor sensor = createSensor();
		
		//Create Data
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Data data = new Data(new Date(0), new Date(10), sensor);
		
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
		assertNotNull("Relation to Sensor not persisted",d.getSensor());
		
		assertEquals("Sensors not equally", d.getSensor(), sensor);
		
		em.remove(d);
		em.getTransaction().commit();
		em.close();
		
		removePatient(patient);
		removeSensor(sensor);
	}
	
	@Test
	public void testDataRemovalWithTherapyResult() {
		Patient patient = createPatient();
		Sensor sensor = createSensor();
		
		//Create Data
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Data data = new Data(new Date(0), new Date(10), sensor);
		
		assertEquals("Id should not be set", data.getId(), 0);
		em.persist(data);
		assertTrue("Id should be set", data.getId() != 0);
		em.getTransaction().commit();
		em.close();
		
		//Create Data
		em = emf.createEntityManager();
		em.getTransaction().begin();
		
		Therapy therapy = new Therapy("test",patient);
		TherapyResult therapyResult = new TherapyResult("test",data, therapy);
		
		em.persist(therapy);
		em.persist(therapyResult);
		
		em.getTransaction().commit();
		em.close();
		
		removePatient(patient);
		removeSensor(sensor);
	}

}
