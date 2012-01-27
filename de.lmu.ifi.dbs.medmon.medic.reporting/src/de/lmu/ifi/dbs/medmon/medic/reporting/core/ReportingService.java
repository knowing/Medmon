package de.lmu.ifi.dbs.medmon.medic.reporting.core;

import static java.nio.file.Files.walkFileTree;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import de.lmu.ifi.dbs.medmon.medic.reporting.service.IReportingService;

public class ReportingService implements IReportingService {

	private final Logger			log				= LoggerFactory.getLogger(IReportingService.class);
	private Path					tempDirectory	= Paths.get(System.getProperty("user.home"), ".medmon", "reporting", ".temp");
	private IResourceStore			resourceStore;
	private Map<String, Browser>	browserMap		= new HashMap<String, Browser>();
	private EngineConfig			engineConfig;

	@Override
	public void registerBrowser(Browser browser, String id) {
		browserMap.put(id, browser);
	}

	@Override
	public void unregisterBrowser(String id) {
		browserMap.remove(id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Path renderReport(String reportId, Map<String, Object> taskParameters, ClassLoader classLoader, List<IJAXBReportData> data)
			throws IOException, BirtProcessingException {

		/* ==== Create Design and Document Paths ==== */
		URL reportDesign = resourceStore.getResource(reportId + ".rptdesign").get();
		Path documentPath = Paths.get(tempDirectory.toString(), reportId + ".rptdocument");

		/* ==== Configure and run task ==== */
		IReportEngineFactory factory = (IReportEngineFactory) org.eclipse.birt.core.framework.Platform
				.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
		IReportEngine reportEngine = factory.createReportEngine(engineConfig);

		try (InputStream reportInputStream = reportDesign.openStream()) {
			IReportRunnable design;
			design = reportEngine.openReportDesign(reportId, reportInputStream);

			IRunTask task = reportEngine.createRunTask(design);

			/* ==== Create Report Data XML files ==== */
			for (IJAXBReportData d : data) {

				String dataFileName = d.getId() + ".xml";
				String schemaFileName = d.getId() + ".xsd";
				String dataParam = d.getId() + "_" + "xml";
				String schemaParam = d.getId() + "_" + "xsd";

				Path reportDataDestPath = Paths.get(tempDirectory.toString(), dataFileName);
				Path reportSchemaDestPath = Paths.get(tempDirectory.toString(), schemaFileName);
				URL reportDataSchemaSourceURL = resourceStore.getResource(schemaFileName).get();

				/* === delete file if exists -> create file -> copy data to file -> tell report about data files ===*/
				Files.deleteIfExists(reportDataDestPath);
				try (OutputStream fileOutputStream = Files.newOutputStream(reportDataDestPath, StandardOpenOption.CREATE_NEW)) {
					d.marshal(fileOutputStream);
					Files.copy(reportDataSchemaSourceURL.openStream(), reportSchemaDestPath, StandardCopyOption.REPLACE_EXISTING);
					task.setParameterValue(dataParam, reportDataDestPath.toString());
					task.setParameterValue(schemaParam, reportSchemaDestPath.toString());
				} catch (IOException e) {
					e.printStackTrace();
					throw e;
				}
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

		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} catch (EngineException e) {
			e.printStackTrace();
			throw new BirtProcessingException("<TODO>");
		} finally {

			/* === Delete Files === */
			for (IJAXBReportData d : data) {

				String dataFileName = d.getId() + ".xml";
				String schemaFileName = d.getId() + ".xsd";

				try {
					Files.deleteIfExists(Paths.get(tempDirectory.toString(), dataFileName));
					Files.deleteIfExists(Paths.get(tempDirectory.toString(), schemaFileName));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return documentPath;
	}

	@Override
	public void renderReportToBrowser(String reportId, String id, Map<String, Object> taskParameters, ClassLoader classLoader,
			List<IJAXBReportData> data) throws BirtProcessingException, IOException {

		Browser browser = browserMap.get(id);
		if (browser == null)
			return;

		Path documentPath = renderReport(reportId, taskParameters, classLoader, data);

		HashMap<String, String> myparms = new HashMap<String, String>();
		myparms.put("SERVLET_NAME_KEY", "run");
		myparms.put("FORMAT_KEY", "html");
		WebViewer.display(documentPath.toString(), browser, myparms);
	}

	@Override
	public void renderReportToPDF(String reportId, Path destPath, Map<String, Object> taskParameters, ClassLoader classLoader,
			List<IJAXBReportData> data) {

	}

	protected void activate() {

		try {
			Files.createDirectories(tempDirectory);
			engineConfig = new EngineConfig();
			org.eclipse.birt.core.framework.Platform.startup(engineConfig);

		} catch (IOException | BirtException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		log.debug("ReportingService activated");
	}

	protected void deactivate() {
		try {
			walkFileTree(tempDirectory, new DeleteDirectoryVisitor());
			org.eclipse.birt.core.framework.Platform.shutdown();
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
