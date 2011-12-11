package de.lmu.ifi.dbs.medmon.base.ui.wizard.pages;

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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.knowing.core.model.IDataProcessingUnit;
import de.lmu.ifi.dbs.knowing.core.model.INode;
import de.lmu.ifi.dbs.knowing.core.model.IProperty;
import de.lmu.ifi.dbs.knowing.core.model.NodeType;
import de.lmu.ifi.dbs.knowing.core.processing.INodeProperties;
import de.lmu.ifi.dbs.knowing.core.service.IEvaluateService;
import de.lmu.ifi.dbs.knowing.core.swt.view.PresenterView;
import de.lmu.ifi.dbs.medmon.base.ui.Activator;
import de.lmu.ifi.dbs.medmon.base.ui.filter.DPUInputFilter;
import de.lmu.ifi.dbs.medmon.base.ui.filter.DPUSearchFilter;
import de.lmu.ifi.dbs.medmon.base.ui.viewer.DPUTableViewer;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.IValidationPage;
import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;
import de.lmu.ifi.dbs.medmon.sensor.core.IConverter;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

public class SelectAndConfigureDPUPage extends WizardPage implements IValidationPage {

	private final Logger	log	= LoggerFactory.getLogger(Activator.PLUGIN_ID);

	private Text			textSearch;
	private DPUTableViewer	dpuViewer;

	/**
	 * Create the wizard.
	 */
	public SelectAndConfigureDPUPage() {
		super("dpu-configuration");
		setTitle("Klassifikationsverfahren");
		setDescription("Klassifikationsverfahren auswaehlen");
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(2, false));

		Label lableSearch = new Label(container, SWT.NONE);
		lableSearch.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lableSearch.setText("Suche");

		// Search filter
		final DPUSearchFilter dpuSearchFilter = new DPUSearchFilter();

		// Filters all loader
		final DPUInputFilter dpuInputFilter = new DPUInputFilter();

		// Filters all saver

		textSearch = new Text(container, SWT.BORDER);
		textSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textSearch.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				dpuSearchFilter.setSearchText(textSearch.getText());
				dpuViewer.refresh();
			}
		});

		dpuViewer = new DPUTableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		dpuViewer.addFilter(dpuSearchFilter);
		dpuViewer.addFilter(dpuInputFilter);
		Table table = dpuViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
	}

	/**
	 * <p>
	 * Configures the selected DPU and exectutes it with data from db.
	 * </p>
	 * 
	 * @param patient
	 * @param data
	 * @throws IOException
	 * @return configured DPU or null
	 */
	public void configureAndExecuteDPU(Patient patient, Data data) throws IOException {
		IPatientService patientService = Activator.getPatientService();
		Path execPath = patientService.locateDirectory(patient, IPatientService.ROOT);
		Path inputFile = patientService.locateFilename(data, IPatientService.ROOT);
		
		IDataProcessingUnit dpu = configureDPU(patient,inputFile);

		// TODO data is detached. Will this work?
		Map<String, OutputStream> outputMap = createOutputMap(patient, data, patientService);

		executeDPU(execPath, dpu, outputMap);

	}

	
	/**
	 * <p>
	 * Configures the selected DPU and executes it with data from sensor.
	 * </p>
	 * @param patient
	 * @param sensor
	 * @param input
	 * @throws IOException
	 */
	public void configureAndExecuteDPU(Patient patient, ISensor sensor, URI input) throws IOException {
		IPatientService patientService = Activator.getPatientService();
		Path execPath = patientService.locateDirectory(patient, IPatientService.ROOT);
		Path inputFile = Paths.get(input);
		
		IDataProcessingUnit dpu = configureDPU(patient,inputFile);

		// TODO data is detached. Will this work?
		Map<String, OutputStream> outputMap = createOutputMap(patient, sensor, input, patientService);

		executeDPU(execPath, dpu, outputMap);
	}

	private void executeDPU(Path execPath, IDataProcessingUnit dpu, Map<String, OutputStream> outputMap) {
		try {
			IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(PresenterView.ID());
			PresenterView pView = (PresenterView) view;
			pView.clearTabs();
			// TODO Create own UIFactory!
			IEvaluateService evalService = Activator.getEvaluationService();
			evalService.evaluate(dpu, pView.uifactory(), execPath.toUri(), mapAsScalaMap(new HashMap<String, InputStream>()),
					mapAsScalaMap(outputMap));
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>
	 * Configures the selected DPU and executes it.
	 * </p>
	 * 
	 * @param patient
	 * @param data
	 * @throws IOException
	 * @return configured DPU or null
	 */
	private IDataProcessingUnit configureDPU(Patient patient, Path inputFile) throws IOException {
		if (dpuViewer.getSelection().isEmpty())
			return null;

		IStructuredSelection selection = (IStructuredSelection) dpuViewer.getSelection();
		IDataProcessingUnit originalDPU = (IDataProcessingUnit) selection.getFirstElement();
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
				
				// Set Path absolute or relative. Depend if URI or Data was used to create inputFile
				if(!absolutePathSet) {
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
						log.debug("Set Processor[" + node.getId().getContent() + "] (DE)SERIALIZE to " + resolvedFile + " [" + p.getValue().getContent() + "]");
					}
				}
			}
		}
		return dpu;
	}
	
	private Map<String, OutputStream> createOutputMap(Patient patient, Data data, IPatientService patientService) throws IOException {
		HashMap<String, OutputStream> outputMap = new HashMap<String, OutputStream>();
		OutputStream outputStream = patientService.store(patient, data.getSensor(), IPatientService.RESULT, data.getFrom(), data.getTo()).outputStream;

		// IPatientService.RESULT -> document this somewhere, very important!
		outputMap.put(IPatientService.RESULT, outputStream);
		return outputMap;
	}
	
	private Map<String, OutputStream> createOutputMap(Patient patient, ISensor sensor, URI input, IPatientService patientService) throws IOException {
		HashMap<String, OutputStream> outputMap = new HashMap<String, OutputStream>();
		
		IConverter converter = Activator.getSensorManagerService().createConverter(sensor);
		Sensor sensorEntity = Activator.getSensorManagerService().loadSensorEntity(sensor);
		Interval interval = converter.getInterval();
		OutputStream outputStream = patientService.store(patient, sensorEntity, IPatientService.RESULT, interval.getStart().toDate(), interval.getEnd().toDate()).outputStream;

		// IPatientService.RESULT -> document this somewhere, very important!
		outputMap.put(IPatientService.RESULT, outputStream);
		return outputMap;
	}
	
	@Override
	public void checkContents() {
		
	}
}
