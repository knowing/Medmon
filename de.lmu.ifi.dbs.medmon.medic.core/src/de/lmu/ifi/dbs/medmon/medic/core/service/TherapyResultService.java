package de.lmu.ifi.dbs.medmon.medic.core.service;

import static scala.collection.JavaConversions.mapAsScalaMap;

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

import org.eclipse.swt.widgets.Composite;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.knowing.core.factory.UIFactory;
import de.lmu.ifi.dbs.knowing.core.model.IDataProcessingUnit;
import de.lmu.ifi.dbs.knowing.core.model.INode;
import de.lmu.ifi.dbs.knowing.core.model.IProperty;
import de.lmu.ifi.dbs.knowing.core.model.NodeType;
import de.lmu.ifi.dbs.knowing.core.processing.INodeProperties;
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
 * @version 0.1
 * @since 26.12.2011
 */
public class TherapyResultService implements ITherapyResultService {

	private final Logger				log			= LoggerFactory.getLogger(ITherapyResultService.class);

	/** 1..1 relation */
	private IPatientService				patientService;
	/** 1..1 relation */
	private IEvaluateService			evaluateService;

	private IEntityManagerService		entityManagerService;

	/** 0..n relation */
	private List<UIFactory<Composite>>	uiFactories	= new ArrayList<UIFactory<Composite>>();

	// private List<IModelStore> modelStorie = new ArrayList<IModelStore>();

	@Override
	public TherapyResult createTherapyResult(IDataProcessingUnit dpu, Patient patient, Therapy therapy, Data data) throws Exception {
		// TODO Try/Catch block to rollback actions on failure

		// Resolve data location -> inputfile
		Path execPath = patient.toPath();
		Path inputFile = data.toPath();

		// Configure properties to run with inputFile
		// IDataProcessingUnit configuredDPU = configureDPU(dpu, patient,
		// inputFile);

		Properties parameters = new Properties();
		parameters.setProperty("sdr-file", inputFile.toString());

		// Generate Data entity which stores the result
		// Data resultData = createData(patient, data);
		// FIXME
		Data resultData = null;
		Map<String, OutputStream> outputMap = createOutputMap(Files.newOutputStream(resultData.toPath()));

		// Finally run the DPU
		// executeDPU(execPath, configuredDPU, outputMap);
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

		// IDataProcessingUnit configuredDPU = configureDPU(dpu, patient,
		// inputFile);

		Properties parameters = new Properties();
		parameters.setProperty("sdr-file", inputFile.toString());
		// parameters.setProperty("arff-output", trgFile);

		// SDR Classification No UI No Reclassification | SDR Classification No
		// UI
		// SDR Classification To ACData No Reclassification
		// eval.evaluate(dpu, execPath.toUri(), dlg.getUiFactory(),
		// dlg.getSystem(), parameters, null, null);

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

	private void executeDPU(Path execPath, IDataProcessingUnit dpu, Map<String, OutputStream> outputMap) throws Exception {
		// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(PresenterView.ID());
		// TODO Create own UIFactory! Check if uiFactories aren't empty, etc
		if (uiFactories.isEmpty())
			throw new Exception("No UIFactory. Unable to display results.");

		evaluateService.evaluate(dpu, execPath.toUri(), uiFactories.get(0), null, null, mapAsScalaMap(new HashMap<String, InputStream>()),
				mapAsScalaMap(outputMap));
	}

	/**
	 * <p>
	 * Configures the selected DPU:
	 * <li>Remove all FILE, DIR, URL properties on NodeType.LOADER</li>
	 * <li>Remove all FILE, DIR, URL properties on NodeType.SAVER</li>
	 * <li>Set (DE)SERIALIZE property</li>
	 * </p>
	 * 
	 * @param originalDPU
	 * @param patient
	 * @param data
	 * @throws IOException
	 * @return configured DPU or null
	 */
	private IDataProcessingUnit configureDPU(IDataProcessingUnit originalDPU, Patient patient, Path inputFile) throws IOException {
		IDataProcessingUnit dpu = IDataProcessingUnit.TYPE.instantiate();
		dpu.copy(originalDPU);

		// Set properties
		for (INode node : dpu.getNodes()) {

			// Loader -> replace file with locateFilename(data, RAW)
			if (node.getType().getContent().equals(NodeType.LOADER)) {
				// This is not very handsome. Store properties to remove and
				// then remove them.
				List<IProperty> toRemove = new ArrayList<IProperty>();
				boolean filePropertySet = false;
				boolean absolutePathSet = false;
				String filePropertyValue = inputFile.toString();
				for (IProperty p : node.getProperties()) {
					String key = p.getKey().getContent();
					if (key.equals(INodeProperties.FILE())) {
						p.setValue(filePropertyValue);
						filePropertySet = true;
					} else if (key.equals(INodeProperties.ABSOLUTE_PATH())) {
						p.setValue(String.valueOf(inputFile.isAbsolute()));
						absolutePathSet = true;
					} else if (key.equals(INodeProperties.DIR()) || key.equals(INodeProperties.URL())) {
						toRemove.add(p);
					}
				}

				// Actual removal.
				for (IProperty p : toRemove) {
					node.getProperties().remove(p);
				}

				// Test if file is set
				if (!filePropertySet) {
					IProperty fileProperty = node.getProperties().insert();
					fileProperty.setKey(INodeProperties.FILE());
					fileProperty.setValue(filePropertyValue);
				}

				// Set Path absolute or relative. Depend if URI or Data was used
				// to create inputFile
				if (!absolutePathSet) {
					IProperty absoluteProperty = node.getProperties().insert();
					absoluteProperty.setKey(INodeProperties.ABSOLUTE_PATH());
					absoluteProperty.setValue(String.valueOf(inputFile.isAbsolute()));
				}

				log.debug("Set Loader[" + node.getId().getContent() + "] FILE to " + filePropertyValue);

			} else if (node.getType().getContent().equals(NodeType.SAVER)) {
				// This is not very handsome. Store properties to remove and
				// then remove them.
				List<IProperty> toRemove = new ArrayList<IProperty>();
				for (IProperty p : node.getProperties()) {
					String key = p.getKey().getContent();
					if (key.equals(INodeProperties.FILE()) || key.equals(INodeProperties.URL())) {
						toRemove.add(p);
					}
				}
				// Actual removal.
				for (IProperty p : toRemove) {
					node.getProperties().remove(p);
					log.debug("Removed property " + p.getKey().getContent() + " from " + node.getId().getContent());
				}

			} else {
				// Change serialize/deserialize property
				for (IProperty p : node.getProperties()) {
					String key = p.getKey().getContent();
					if (key.equals(INodeProperties.SERIALIZE()) || key.equals(INodeProperties.DESERIALIZE())) {
						String serializePath = p.getValue().getContent();
						Path serializeFile = Paths.get(serializePath).getFileName();
						Path resolvedFile = Paths.get(Data.TRAIN).resolve(serializeFile);
						p.setValue(resolvedFile.toString());
						log.debug("Set Processor[" + node.getId().getContent() + "] (DE)SERIALIZE to " + resolvedFile + " ["
								+ p.getValue().getContent() + "]");
					}
				}
			}
		}
		return dpu;
	}

	private Map<String, OutputStream> createOutputMap(OutputStream outputStream) throws IOException {
		HashMap<String, OutputStream> outputMap = new HashMap<String, OutputStream>();
		// Data.RESULT -> document this somewhere, very important!
		outputMap.put(Data.RESULT, outputStream);
		return outputMap;
	}

	/*
	 * private Data createData(Patient patient, Data data) throws IOException {
	 * return patientService.store(patient, data.getSensor(), Data.RESULT,
	 * data.getFrom(), data.getTo()); }
	 */

	private Data createData(Patient patient, ISensor sensor) throws IOException {
		return patientService.store(patient, sensor, Data.RESULT);
	}

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
