package de.lmu.ifi.dbs.medmon.medic.core.service;

import static scala.collection.JavaConversions.mapAsScalaMap;

import java.awt.Composite;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.knowing.core.factory.UIFactory;
import de.lmu.ifi.dbs.knowing.core.model.IDataProcessingUnit;
import de.lmu.ifi.dbs.knowing.core.service.IEvaluateService;
import de.lmu.ifi.dbs.medmon.database.entity.Data;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.database.entity.Therapy;
import de.lmu.ifi.dbs.medmon.database.entity.TherapyResult;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;
import de.lmu.ifi.dbs.medmon.services.IEntityManagerService;
import de.lmu.ifi.dbs.medmon.services.IPatientService;
import de.lmu.ifi.dbs.medmon.services.ITherapyResultService;

/**
 * Starts the evaluation process with knowing and generates a TherapyResult
 * entity.
 * 
 * @author Nepomuk Seiler
 * @version 0.3
 * @since 26.12.2011
 */
public class TherapyResultService implements ITherapyResultService {

    private final Logger log = LoggerFactory.getLogger(ITherapyResultService.class);

    /** 1..1 relation */
    private IPatientService patientService;
    /** 1..1 relation */
    private IEvaluateService evaluateService;

    private IEntityManagerService entityManagerService;

    /** 0..n relation */
    private final List<UIFactory<?>> uiFactories = new ArrayList<UIFactory<?>>();

    @Override
    public TherapyResult createTherapyResult(IDataProcessingUnit dpu, Patient patient, Therapy therapy, Data data) throws Exception {
        // TODO Try/Catch block to rollback actions on failure

        // Resolve data location -> inputfile
        Path execPath = patient.toPath();
        Path inputFile = data.toPath();

        Properties parameters = new Properties();
        parameters.setProperty("sdr-file", inputFile.toString());

        // Generate Data entity which stores the result
        Data resultData = createData(patient, data);
        Map<String, OutputStream> outputMap = createOutputMap(Files.newOutputStream(resultData.toPath()));

        // Finally run the DPU
        evaluateService.evaluate(dpu, execPath.toUri(), uiFactories.get(0), null, parameters,
                mapAsScalaMap(new HashMap<String, InputStream>()), mapAsScalaMap(outputMap));

        // Create the TherapyResultEntity with the data entity
        EntityManager tempEm = entityManagerService.createEntityManager();
        tempEm.getTransaction().begin();
        TherapyResult result = new TherapyResult("<Neues Therapieergebnis>", resultData, therapy);
        tempEm.persist(result);
        tempEm.getTransaction().commit();
        tempEm.close();

        return result;
    }

    @Override
    public TherapyResult createTherapyResult(IDataProcessingUnit dpu, Patient patient, Therapy therapy, ISensor sensor, URI input)
            throws Exception {
        // TODO Try/Catch block to rollback actions on failure
        Path execPath = patient.toPath();
        Path inputFile = Paths.get(input);

        Properties parameters = new Properties();
        parameters.setProperty("sdr-file", inputFile.toString());

        Data data = createData(patient, sensor);
        Map<String, OutputStream> outputMap = createOutputMap(Files.newOutputStream(data.toPath()));

        // executeDPU(execPath, dpu, outputMap);
        evaluateService.evaluate(dpu, execPath.toUri(), uiFactories.get(0), null, parameters,
                mapAsScalaMap(new HashMap<String, InputStream>()), mapAsScalaMap(outputMap));
        // Create the TherapyResultEntity with the data entity
        EntityManager tempEm = entityManagerService.createEntityManager();
        tempEm.getTransaction().begin();
        TherapyResult result = new TherapyResult("<Neues Therapieergebnis>", data, therapy);
        tempEm.persist(result);
        tempEm.getTransaction().commit();
        tempEm.close();

        return result;
    }

    private Map<String, OutputStream> createOutputMap(OutputStream outputStream) throws IOException {
        HashMap<String, OutputStream> outputMap = new HashMap<String, OutputStream>();
        // Data.RESULT -> document this somewhere, very important!
        outputMap.put(Data.RESULT, outputStream);
        return outputMap;
    }

    private Data createData(Patient patient, Data data) throws IOException {
        return patientService.store(patient, data);
    }

    private Data createData(Patient patient, ISensor sensor) throws IOException {
        return patientService.store(patient, sensor, Data.RESULT);
    }

    /* ============================================== */
    /* ========== OSGi declarative service ========== */
    /* ============================================== */

    protected void activate(ComponentContext context) {
        log.debug("TherapyResultSerivce activated");
    }

    protected void bindPatientService(IPatientService patientService) {
        this.patientService = patientService;
    }

    protected void unbindPatientService(IPatientService patientService) {
        this.patientService = null;
    }

    protected void bindEvaluateService(IEvaluateService evaluateService) {
        this.evaluateService = evaluateService;
    }

    protected void unbindEvaluateService(IEvaluateService evaluateService) {
        this.evaluateService = null;
    }

    protected void bindEntityManagerService(IEntityManagerService entityManagerService) {
        this.entityManagerService = entityManagerService;
    }

    protected void unbindEntityManagerService(IEntityManagerService entityManagerService) {
        this.entityManagerService = null;
    }

    protected void bindUiFactories(UIFactory<Composite> uiFactory) {
        uiFactories.add(uiFactory);
    }

    protected void unbindUiFactories(UIFactory<Composite> uiFactory) {
        uiFactories.remove(uiFactory);
    }
}
