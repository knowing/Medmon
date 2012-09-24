package de.lmu.ifi.dbs.medmon.medic.core.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import de.lmu.ifi.dbs.medmon.database.entity.Data;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.database.entity.Sensor;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensorDriver;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensorListener;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensorManager;
import de.lmu.ifi.dbs.medmon.sensor.core.SensorEvent;
import de.lmu.ifi.dbs.medmon.services.IEntityManagerService;
import de.lmu.ifi.dbs.medmon.services.IPatientService;

/**
 * 
 * @author Nepomuk Seiler
 * 
 */
public class PatientService implements IPatientService {

    private final Logger log = LoggerFactory.getLogger(IPatientService.class);

    private IEntityManagerService entityManagerService;
    private ISensorManager sensorManager;

    @Override
    public Data store(Patient patient, ISensor sensor, String type, String file) throws IOException {
        if (sensor.getDriver() == null)
            throw new IOException("No driver found for sensor " + sensor.getName());

        ISensorDriver driver = sensor.getDriver();
        Interval interval = null;
        try (InputStream in = Files.newInputStream(Paths.get(file))) {
            interval = driver.getInterval(in);
        }

        Data data = createDataEntity(patient, sensor, type, interval);

        if (type.equals(Data.RESULT) || type.equals(Data.TRAIN)) {
            // Copy converted
            try (InputStream in = Files.newInputStream(Paths.get(file)); OutputStream out = Files.newOutputStream(data.toPath())) {
                interval = driver.getInterval(in);
                Instances instances = driver.getData(in);
                ArffSaver saver = new ArffSaver();
                saver.setInstances(instances);
                saver.setDestination(out);
                saver.writeBatch();
            }

        } else {
            // Copy raw
            try (InputStream in = sensor.getDataInputStream()) {
                Files.copy(in, data.toPath());
            }
        }

        return null;
    }

    @Override
    public Data store(Patient patient, ISensor sensor, String type) throws IOException {
        if (sensor.getDriver() == null)
            throw new IOException("No driver found for sensor " + sensor.getName());

        Interval interval = sensor.getInterval();

        Data data = createDataEntity(patient, sensor, type, interval);

        if (type.equals(Data.RESULT) || type.equals(Data.TRAIN)) {
            // Copy converted
            Instances instances = sensor.getData();
            ArffSaver saver = new ArffSaver();
            saver.setInstances(instances);
            try (OutputStream out = Files.newOutputStream(data.toPath())) {
                saver.setDestination(out);
                saver.writeBatch();
            }
        } else {
            // Copy raw
            try (InputStream in = sensor.getDataInputStream()) {
                Files.copy(in, data.toPath());
            }
        }

        return data;

    }

    private Data createDataEntity(Patient patient, ISensor sensor, String type, Interval interval) {
        EntityManager em = entityManagerService.createEntityManager();
        em.getTransaction().begin();
        // TODO load sensor entity - test
        Sensor entity = em.find(Sensor.class, sensor.getId());
        Data data = new Data(interval.getStart().toDate(), interval.getEnd().toDate(), entity);
        data.setPatient(patient);
        data.setType(type);
        em.persist(data);
        em.getTransaction().commit();
        em.close();
        return data;
    }

    protected void activate(Map<String, Object> properties) {
        log.debug("PatientService activated. Properties: " + properties);
        sensorManager.addListener(new ISensorListener() {

            @Override
            public void sensorChanged(SensorEvent event) {
                syncDatabase();
            }
        });
        syncDatabase();
    }

    private void syncDatabase() {
        List<ISensor> sensors = sensorManager.getSensors();
        EntityManager em = entityManagerService.createEntityManager();
        em.getTransaction().begin();
        for (ISensor sensor : sensors) {
            Sensor entity = em.find(Sensor.class, sensor.getId());
            if (entity == null) {
                Sensor newEntity = new Sensor(sensor.getId(), sensor.getName(), sensor.getSerial());
                em.persist(newEntity);
            }
        }
        em.getTransaction().commit();
        em.close();
    }

    protected void bindEntityManager(IEntityManagerService service) {
        entityManagerService = service;
    }

    protected void unbindEntityManager(IEntityManagerService service) {
        entityManagerService = null;
    }

    protected void bindSensorManager(ISensorManager service) {
        sensorManager = service;
    }

    protected void unbindSensorManager(ISensorManager service) {
        sensorManager = null;
    }

}
