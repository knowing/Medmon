package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import static scala.collection.JavaConversions.mapAsScalaMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.joda.time.Interval;
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
import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.database.model.Therapy;
import de.lmu.ifi.dbs.medmon.database.model.TherapyResult;
import de.lmu.ifi.dbs.medmon.medic.core.service.IDBModelService;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;
import de.lmu.ifi.dbs.medmon.medic.core.service.ISensorManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.service.ITherapyResultService;
import de.lmu.ifi.dbs.medmon.medic.core.util.DataStoreOutput;
import de.lmu.ifi.dbs.medmon.sensor.core.IConverter;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

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
	private ISensorManagerService		sensorManagerService;
	/** 1..1 relation */
	private IEvaluateService			evaluateService;
	/** 1..1 relation */
	private IDBModelService				dbModelService;

	/** 0..n relation */
	private List<UIFactory<Composite>>	uiFactories	= new ArrayList<UIFactory<Composite>>();

	// private List<IModelStore> modelStorie = new ArrayList<IModelStore>();

	@Override
	public TherapyResult createTherapyResult(IDataProcessingUnit dpu, Patient patient, Therapy therapy, Data data) throws Exception {
		// TODO Try/Catch block to rollback actions on failure

		Path execPath = patientService.locateDirectory(patient, IPatientService.ROOT);
		Path inputFile = patientService.locateFilename(data, IPatientService.ROOT);

		IDataProcessingUnit configuredDPU = configureDPU(dpu, patient, inputFile);

		// TODO data is detached. Will this work?
		DataStoreOutput store = createData(patient, data);
		Map<String, OutputStream> outputMap = createOutputMap(store.outputStream);

		executeDPU(execPath, configuredDPU, outputMap);

		return dbModelService.createTherapyResult(store.dataEntity, therapy);
	}

	@Override
	public TherapyResult createTherapyResult(IDataProcessingUnit dpu, Patient patient, Therapy therapy, ISensor sensor, URI input)
			throws Exception {
		// TODO Try/Catch block to rollback actions on failure
		Path execPath = patientService.locateDirectory(patient, IPatientService.ROOT);
		Path inputFile = Paths.get(input);

		IDataProcessingUnit configuredDPU = configureDPU(dpu, patient, inputFile);

		// TODO data is detached. Will this work?
		DataStoreOutput store = createData(patient, sensor, input);
		Map<String, OutputStream> outputMap = createOutputMap(store.outputStream);

		executeDPU(execPath, configuredDPU, outputMap);
		return dbModelService.createTherapyResult(store.dataEntity, therapy);
	}

	private void executeDPU(Path execPath, IDataProcessingUnit dpu, Map<String, OutputStream> outputMap) throws Exception {
		// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(PresenterView.ID());
		// TODO Create own UIFactory! Check if uiFactories aren't empty, etc
		evaluateService.evaluate(dpu, execPath.toUri(), uiFactories.get(0), mapAsScalaMap(new HashMap<String, InputStream>()),
				mapAsScalaMap(outputMap));

	}

	/**
	 * <p>
	 * Configures the selected DPU and executes it.
	 * </p>
	 * 
	 * @param originalDDPU
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
					IProperty fileProperty = node.getProperties().addNewElement();
					fileProperty.setKey(INodeProperties.FILE());
					fileProperty.setValue(filePropertyValue);
				}

				// Set Path absolute or relative. Depend if URI or Data was used
				// to create inputFile
				if (!absolutePathSet) {
					IProperty absoluteProperty = node.getProperties().addNewElement();
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
						Path resolvedFile = Paths.get(IPatientService.TRAIN).resolve(serializeFile);
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
		// IPatientService.RESULT -> document this somewhere, very important!
		outputMap.put(IPatientService.RESULT, outputStream);
		return outputMap;
	}

	private DataStoreOutput createData(Patient patient, Data data) throws IOException {
		return patientService.store(patient, data.getSensor(), IPatientService.RESULT, data.getFrom(), data.getTo());
	}

	private DataStoreOutput createData(Patient patient, ISensor sensor, URI input) throws IOException {
		IConverter converter = sensorManagerService.createConverter(sensor);
		Sensor sensorEntity = sensorManagerService.loadSensorEntity(sensor);
		Interval interval = converter.getInterval();
		return patientService
				.store(patient, sensorEntity, IPatientService.RESULT, interval.getStart().toDate(), interval.getEnd().toDate());
	}
	
	protected void activate(ComponentContext context)  {
		log.debug("TherapyResultSerive started");
	}
	
	protected void bindPatientService(IPatientService patientService) {
		this.patientService = patientService;
	}
	
	protected void unbindPatientService(IPatientService patientService) {
		this.patientService = null;
	}
	
	protected void bindSensorManagerService(ISensorManagerService sensorManagerService) {
		this.sensorManagerService = sensorManagerService;
	}
	
	protected void unbindSensorManagerService(ISensorManagerService sensorManagerService) {
		this.sensorManagerService = null;
	}
	
	protected void bindEvaluateService(IEvaluateService evaluateService) {
		this.evaluateService = evaluateService;
	}
	
	protected void unbindEvaluateService(IEvaluateService evaluateService) {
		this.evaluateService = null;
	}
	
	protected void bindDbModelService(IDBModelService dbModelService) {
		this.dbModelService = dbModelService;
	}
	
	protected void unbindDbModelService(IDBModelService dbModelService) {
		this.dbModelService = null;
	}
	
	protected void bindUiFactories(UIFactory<Composite> uiFactory) {
		uiFactories.add(uiFactory);
	}
	
	protected void unbindUiFactories(UIFactory<Composite> uiFactory) {
		uiFactories.remove(uiFactory);
	}
}
