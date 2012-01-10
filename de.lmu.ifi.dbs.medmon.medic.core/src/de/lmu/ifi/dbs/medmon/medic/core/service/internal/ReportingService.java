package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunTask;

import de.lmu.ifi.dbs.medmon.medic.core.service.IReportingService;
import de.lmu.ifi.dbs.medmon.medic.reporting.core.IReportData;

public class ReportingService implements IReportingService {

	@SuppressWarnings("unchecked")
	@Override
	public void renderReport(URL reportDesign, URL reportSchema, IReportData data, Map<String, Object> taskParameters, Path reportOutput,
			ClassLoader classLoader) {

		// the temporary schema file to be generated
		Path reportSchemaPath;
		try {
			reportSchemaPath = Paths.get(System.getProperty("user.home"), ".medmon", "reporting", "temp_schema.xsd");

			FileOutputStream schemaOutputStream = new FileOutputStream(reportSchemaPath.toFile());
			InputStream schemaInputStream = reportSchema.openStream();

			byte[] buffer = new byte[0xFFFF];
			for (int len; (len = schemaInputStream.read(buffer)) != -1;)
				schemaOutputStream.write(buffer, 0, len);

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// create the temporary data file
		Path dataPath = Paths.get(System.getProperty("user.home"), ".medmon", "reporting", "temp_data.xml");

		data.marshal(dataPath);

		// start BIRT
		EngineConfig config = new EngineConfig();
		IReportEngine reportEngine;

		try {
			org.eclipse.birt.core.framework.Platform.startup(config);
			IReportEngineFactory factory = (IReportEngineFactory) org.eclipse.birt.core.framework.Platform
					.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
			reportEngine = factory.createReportEngine(config);

		} catch (BirtException e) {
			e.printStackTrace();
			return;
		}

		// load the design
		try {
			IReportRunnable design = reportEngine.openReportDesign(reportDesign.openStream());
			IRunTask task = reportEngine.createRunTask(design);

			if (taskParameters != null)
				for (Entry<String, Object> e : taskParameters.entrySet()) {
					task.setParameterValue(e.getKey(), e.getValue());
				}

			task.setParameterValue("dataFile", dataPath.toString());
			task.setParameterValue("schemaFile", reportSchemaPath.toString());

			task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, classLoader);
			task.run(reportOutput.toString());
			task.close();

		} catch (EngineException | IOException e) {
			e.printStackTrace();
			return;
		}
	}
}
