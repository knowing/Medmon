package de.lmu.ifi.dbs.medmon.medic.reporting.core;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.walkFileTree;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunTask;
import org.eclipse.birt.report.viewer.utilities.WebViewer;
import org.eclipse.swt.browser.Browser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.knowing.core.service.IResourceStore;
import de.lmu.ifi.dbs.medmon.medic.core.util.DeleteDirectoryVisitor;
import de.lmu.ifi.dbs.medmon.medic.reporting.data.IJAXBReportData;
import de.lmu.ifi.dbs.medmon.medic.reporting.data.PatientReportData;
import de.lmu.ifi.dbs.medmon.medic.reporting.service.IReportingService;

public class ReportingService implements IReportingService {

	private final Logger	log				= LoggerFactory.getLogger(IReportingService.class);
	private Path			tempDirectory	= Paths.get(System.getProperty("user.home"), ".medmon", "reporting", ".temp");
	private IResourceStore	resourceStore;

	/**
	 * NOTE ! NOTE ! NOTE ! NOTE ! NOTE ! NOTE ! NOTE ! NOTE ! NOTE ! NOTE
	 * 
	 * didn't like the Files.createTemp... gear
	 * Now "user.home/.medmon/reporting/.temp" is created by the service and removed afterwards
	 * via activator and deactivator.
	 * 
	 * also i thought about the renderProvidedreport ... but i think renderReport() is generic enough
	 * correct me if i'm wrong
	 * 
	 * UI currently uses renderReportToBrowser() to minimize code.
	 * 
	 * check it out for yourself
	 * 
	 * NOTE ! NOTE ! NOTE ! NOTE ! NOTE ! NOTE ! NOTE ! NOTE ! NOTE ! NOTE
	 */
	
	@SuppressWarnings("unchecked")
	@Override
	public Path renderReport(String reportId, Map<String, Object> taskParameters, ClassLoader classLoader, List<IJAXBReportData> data) {

		/* ==== Create Design and Document Paths ==== */
		URL reportDesign = resourceStore.getResource(reportId + ".rptdesign").get();
		Path documentPath = Paths.get(tempDirectory.toString(), reportId + ".rptdocument");

		/* ==== Startup BIRT platform ==== */
		EngineConfig config = new EngineConfig();
		try {
			org.eclipse.birt.core.framework.Platform.startup(config);
		} catch (BirtException e1) {
			e1.printStackTrace();
			return null;
		}

		/* ==== Configure and run task ==== */
		IReportEngineFactory factory = (IReportEngineFactory) org.eclipse.birt.core.framework.Platform
				.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
		IReportEngine reportEngine = factory.createReportEngine(config);

		IReportRunnable design;
		try {
			design = reportEngine.openReportDesign(reportId, reportDesign.openStream());
		} catch (EngineException | IOException e1) {
			e1.printStackTrace();
			return null;
		}
		IRunTask task = reportEngine.createRunTask(design);

		/* ==== Create Report Data XML files ==== */
		for (IJAXBReportData d : data) {

			String dataFileName = reportId + "." + d.getId() + ".xml";
			String schemaFileName = reportId + "." + d.getId() + ".xsd";
			String dataParam = d.getId() + "_" + "xml";
			String schemaParam = d.getId() + "_" + "xsd";

			Path reportDataDestPath = Paths.get(tempDirectory.toString(), dataFileName);
			Path reportSchemaDestPath = Paths.get(tempDirectory.toString(), schemaFileName);
			URL reportDataSchemaSourceURL = resourceStore.getResource(schemaFileName).get();

			marshallReportData(reportDataDestPath, d);

			try {
				Files.copy(reportDataSchemaSourceURL.openStream(), reportSchemaDestPath, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}

			task.setParameterValue(dataParam, reportDataDestPath.toString());
			task.setParameterValue(schemaParam, reportSchemaDestPath.toString());
		}

		/* ==== Set the rest of the Parameters ==== */

		/* === Render === */
		try {
			task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, classLoader);
			task.run(documentPath.toString());
			task.close();
		} catch (EngineException e) {
			e.printStackTrace();
			task.close();
		}

		/* === Delete Files === */
		for (IJAXBReportData d : data) {

			String dataFileName = reportId + "." + d.getId() + ".xml";
			String schemaFileName = reportId + "." + d.getId() + ".xsd";

			try {
				Files.deleteIfExists(Paths.get(tempDirectory.toString(), dataFileName));
				Files.deleteIfExists(Paths.get(tempDirectory.toString(), schemaFileName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return documentPath;
	}

	@Override
	public void renderReportToBrowser(String reportId, Browser browser, Map<String, Object> taskParameters, ClassLoader classLoader,
			List<IJAXBReportData> data) {

		Path documentPath =  renderReport(reportId, taskParameters, classLoader, data);

		HashMap<String, String> myparms = new HashMap<String, String>();
		myparms.put("SERVLET_NAME_KEY", "run");
		myparms.put("FORMAT_KEY", "html");
		WebViewer.display(documentPath.toString(), browser, myparms);
	}

	@Override
	public void renderReportToPDF(String reportId, Path destPath, Map<String, Object> taskParameters, ClassLoader classLoader,
			List<IJAXBReportData> data) {

	}

	@Override
	public void marshallReportData(Path dataPath, IJAXBReportData reportData) {
		try {
			JAXBContext context = JAXBContext.newInstance(reportData.getClass());
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(reportData, dataPath.toFile());
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	protected void activate() {
		log.debug("ReportingService activated");
		try {
			Files.createDirectories(tempDirectory);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void deactivate() {
		try {
			walkFileTree(tempDirectory, new DeleteDirectoryVisitor());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void bindResourceStore(IResourceStore store) {
		this.resourceStore = store;
	}

	protected void unbindResourceStore(IResourceStore store) {
		this.resourceStore = null;
	}

}
