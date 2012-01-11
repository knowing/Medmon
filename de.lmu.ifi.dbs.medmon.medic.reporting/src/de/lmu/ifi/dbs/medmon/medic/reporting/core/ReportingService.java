package de.lmu.ifi.dbs.medmon.medic.reporting.core;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunTask;
import org.eclipse.birt.report.viewer.utilities.WebViewer;
import org.eclipse.swt.browser.Browser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.knowing.core.service.IResourceStore;
import de.lmu.ifi.dbs.medmon.medic.reporting.data.PatientReportData;
import de.lmu.ifi.dbs.medmon.medic.reporting.service.IReportingService;

public class ReportingService implements IReportingService {

	private final Logger log = LoggerFactory.getLogger(IReportingService.class);

	private IResourceStore store;

	@Override
	public void renderReport(URL reportDesignURL, URL reportSchemaURL, Map<String, Object> taskParameters, Path reportOutput,
			ClassLoader classLoader) {

	}

	public void displayReport(Browser browser) {

		/* === Creating temporary files === */
		Path tmpReportDataPath = Paths.get("");
		Path tmpReportDataSchemaPath = Paths.get("");
		Path tmpReportDocumentPath = Paths.get("");
		try {
			tmpReportDataPath = Files.createTempFile("medmon.medic.patient_test.", ".xml");
			tmpReportDataSchemaPath = Files.createTempFile("medmon.medic.patient_test.", ".xsd");
			tmpReportDocumentPath = Files.createTempFile("medmon.medic.patient_test.", ".rptdocument");
		} catch (IOException e1) {
			e1.printStackTrace();
			try {
				Files.deleteIfExists(tmpReportDataPath);
				Files.deleteIfExists(tmpReportDataSchemaPath);
				Files.deleteIfExists(tmpReportDocumentPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		URL reportDesign = store.getResource("medmon.medic.patient_test.rptdesign").get();
		URL reportDataSchema = store.getResource("medmon.medic.patient_test.xsd").get();
		log.debug("REPORT DESIGN " + reportDesign);
		log.debug("REPORT DATA SCHEMA " + reportDataSchema);
		try (OutputStream reportDataOutput = Files.newOutputStream(tmpReportDataPath)) {
			/* ==== Create Report Data XML file ==== */
			JAXBContext context = JAXBContext.newInstance(PatientReportData.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			marshaller.marshal(new PatientReportData(), reportDataOutput);

			/* ==== Startup BIRT platform ==== */
			EngineConfig config = new EngineConfig();
			org.eclipse.birt.core.framework.Platform.startup(config);

			/* ==== Configure and run task ==== */
			IReportEngineFactory factory = (IReportEngineFactory) org.eclipse.birt.core.framework.Platform
					.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
			IReportEngine reportEngine = factory.createReportEngine(config);

			IReportRunnable design = reportEngine.openReportDesign("medmon.medic.patient_test.rptdesign", reportDesign.openStream());
			IRunTask task = reportEngine.createRunTask(design);

			/* ==== Generate temporary files and set task parameters ==== */
			task.setParameterValue("dataFile", tmpReportDataPath.toString());

			Files.copy(reportDataSchema.openStream(), tmpReportDataSchemaPath, StandardCopyOption.REPLACE_EXISTING);
			task.setParameterValue("schemaFile", tmpReportDataSchemaPath.toString());

			task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, Activator.class.getClassLoader());
			task.run(tmpReportDocumentPath.toString());
			task.close();

			HashMap<String, String> myparms = new HashMap<String, String>();
			myparms.put("SERVLET_NAME_KEY", "run");
			myparms.put("FORMAT_KEY", "html");
			WebViewer.display(tmpReportDocumentPath.toString(), browser, myparms);

		} catch (JAXBException | IOException | BirtException e) {
			e.printStackTrace();
		} finally {
			try {
				Files.deleteIfExists(tmpReportDataPath);
				Files.deleteIfExists(tmpReportDataSchemaPath);
//				Files.deleteIfExists(tmpReportDocumentPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void activate() {
		log.debug("ReportingService activated");
	}

	protected void bindResourceStore(IResourceStore store) {
		this.store = store;
	}

	protected void unbindResourceStore(IResourceStore store) {
		this.store = null;
	}
}
