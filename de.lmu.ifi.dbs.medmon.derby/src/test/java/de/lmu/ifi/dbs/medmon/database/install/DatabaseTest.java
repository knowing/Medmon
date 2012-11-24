package de.lmu.ifi.dbs.medmon.database.install;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.lmu.ifi.dbs.medmon.database.entity.Data;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.database.entity.Sensor;
import de.lmu.ifi.dbs.medmon.database.entity.Therapy;

public class DatabaseTest {

	protected static EntityManagerFactory emf;

	@BeforeClass
	public static void setUp() throws Exception {
		// Set preference node
		Path dbRoot = Paths.get(System.getProperty("user.dir"), "target", "test-db");
		trace("Test-DB root path is at " + dbRoot);
		Files.createDirectories(dbRoot);
		ConfigurationScope.INSTANCE.getNode("medmon").put("patient", dbRoot.resolve("homefolder").toString());
		Properties properties = new Properties();
		properties.put("javax.persistence.jdbc.url", "jdbc:derby:" + dbRoot.resolve("db") + ";create=true");
		emf = Persistence.createEntityManagerFactory("medmon", properties);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		emf.close();
	}

	@Test
	public void testEntitManagerFactory() {
		assertNotNull(getClass().getClassLoader().getResource("META-INF/persistence.xml"));
		assertNotNull(emf);
		EntityManager em = emf.createEntityManager();
		assertNotNull(em);
		em.close();
	}

	protected Sensor createSensor() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Sensor sensor = new Sensor("TestSensor", "TestSensor:1.0.0", "1.0.0", "txt");
		em.persist(sensor);
		em.getTransaction().commit();
		em.close();
		trace("created sensor " + sensor);
		return sensor;
	}

	protected boolean removeSensor(Sensor sensor) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Sensor s = em.find(Sensor.class, sensor.getId());
		assertNotNull("Sensor not persisted", s);
		em.remove(s);
		em.getTransaction().commit();
		em.close();
		trace("removed sensor " + s);
		return true;
	}

	protected Patient createPatient() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Patient patient = new Patient("John", "Smith", "123A" + Math.random());
		patient.setInsuranceId("xyz123");
		em.persist(patient);
		em.getTransaction().commit();
		em.close();
		trace("created patient " + patient);
		return patient;
	}

	protected boolean removePatient(Patient patient) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Patient p = em.find(Patient.class, patient.getId());
		assertNotNull("Patient not persisted", p);

		em.remove(p);
		em.getTransaction().commit();
		em.close();
		trace("created patient " + p);
		return true;
	}

	protected Data createData(Patient patient, Sensor sensor) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Data data = new Data(new Date(0), new Date(10), sensor);

		assertEquals("Id should not be set", data.getId(), 0);
		em.persist(data);
		assertTrue("Id should be set", data.getId() != 0);
		em.getTransaction().commit();
		em.close();
		trace("created data " + data + " for patient " + patient + ", sensor " + sensor);
		return data;
	}

	protected boolean removeData(Data data, Patient patient, Sensor sensor) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Data d = em.find(Data.class, data.getId());
		assertNotNull("Data not persisted!", d);
		assertNotNull("Relation to Sensor not persisted", d.getSensor());

		assertEquals("Sensors not equally", d.getSensor(), sensor);

		em.remove(d);
		em.getTransaction().commit();
		em.close();
		trace("removed data " + data);
		return true;
	}

	protected void removeTherapy(Therapy therapy, Patient patient) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Therapy t = em.find(Therapy.class, therapy.getId());
		assertNotNull("Therapy not persisted!", t);
		assertNotNull("Relation to Patient not persisted", t.getPatient());

		assertEquals("Patients not equally", t.getPatient(), patient);

		em.remove(t);
		em.getTransaction().commit();
		em.close();
		trace("removed therapy " + therapy);
	}

	protected static void trace(String msg) {
		System.out.println("[DEBUG] " + msg);
	}

}
