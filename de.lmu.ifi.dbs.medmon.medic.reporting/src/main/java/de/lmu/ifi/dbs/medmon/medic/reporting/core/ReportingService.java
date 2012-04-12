package de.lmu.ifi.dbs.medmon.medic.reporting.core;

import static java.nio.file.Files.walkFileTree;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IPDFRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IRenderTask;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.knowing.core.service.IResourceStore;
import de.lmu.ifi.dbs.medmon.database.entity.Report;
import de.lmu.ifi.dbs.medmon.medic.core.util.DeleteDirectoryVisitor;
import de.lmu.ifi.dbs.medmon.medic.reporting.service.IReportingService;

public class ReportingService implements IReportingService {

	private final Logger	log				= LoggerFactory.getLogger(IReportingService.class);
	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
	
	// TODO get this path via the preferences
	private Path			tempDirectory	= Paths.get(System.getProperty("user.home"), ".medmon", "reporting", ".temp");
	private IResourceStore	resourceStore;
	private EngineConfig	engineConfig;

	@SuppressWarnings("unchecked")
	@Override
	public Report renderReport(String reportId, ClassLoader classLoader, Map<String, Object> data, String outputFormat)
			throws IOException, BirtProcessingException {

		/* ==== Create Design and Document Paths ==== */
		URL reportDesign = resourceStore.getResource(reportId + ".rptdesign").get();
		Path documentPath = tempDirectory.resolve(reportId + ".rptdocument");
		Date timestamp = new Date();
		Path outputPath = tempDirectory.resolve(df.format(timestamp) + "_" + reportId + "." + outputFormat);

		/* ==== Configure and run task ==== */
		IReportEngineFactory factory = (IReportEngineFactory) org.eclipse.birt.core.framework.Platform
				.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
		IReportEngine reportEngine = factory.createReportEngine(engineConfig);

		try (InputStream reportInputStream = reportDesign.openStream()) {
			IReportRunnable design = reportEngine.openReportDesign(reportId, reportInputStream);

			//TODO this part will be replaced by POJOs instead of XML files
			IRunTask task = reportEngine.createRunTask(design);
			
			
			/* === Render to temp path and return this path === */
			try {
				Map<String, Object> appContext = task.getAppContext();
				appContext.putAll(data);
				appContext.put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, classLoader);
				task.run(documentPath.toString());
				task.close();

				IReportDocument iReportDocument = reportEngine.openReportDocument(documentPath.toString());
				IRenderOption options = null;
				if(outputFormat.equals("pdf")) {
					options = new PDFRenderOption();
					options.setOption(IPDFRenderOption.PDF_HYPHENATION, true);
					options.setOption(IPDFRenderOption.PDF_TEXT_WRAPPING, true);
					options.setOption(IPDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.FIT_TO_PAGE_SIZE);
					options.setOption(IPDFRenderOption.DPI, 300);
				} else if(outputFormat.equals("html")) {
					options = new HTMLRenderOption();
					options.setBaseURL("UTF-8");
					options.setOption(HTMLRenderOption.URL_ENCODING, "UTF-8");
				}
				options.setOutputFileName(outputPath.toString());
				options.setOutputFormat(outputFormat);

				IRenderTask renderTask = reportEngine.createRenderTask(iReportDocument);
				renderTask.setRenderOption(options);
				renderTask.render();
				iReportDocument.close();
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
		} 
		
		Report report = new Report();
		report.setFile(outputPath.toString());
		report.setFormat(outputFormat);
		report.setReportId(reportId);
		report.setTimestamp(timestamp);
		
		return report;
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
