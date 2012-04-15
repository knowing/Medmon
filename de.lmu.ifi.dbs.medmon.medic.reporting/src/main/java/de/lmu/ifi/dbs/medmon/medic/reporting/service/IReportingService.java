package de.lmu.ifi.dbs.medmon.medic.reporting.service;

import java.io.IOException;
import java.util.Map;

import org.eclipse.birt.report.engine.api.EngineException;

import de.lmu.ifi.dbs.medmon.database.entity.Report;

public interface IReportingService {

	/**
	 * Renders a report to a File
	 * 
	 * @param reportId
	 *            - this method will be using the files named [id].rptdesign,
	 *            [id].xsd and [id].xml to render the report
	 * @param taskParameters
	 *            - passes parameters to the reporting engine
	 * @param classLoader
	 * @param data - a map containing the report data
	 * @return
	 * @throws IOException
	 * @throws EngineException
	 */
	public Report renderReport(String reportId, ClassLoader classLoader, Map<String, Object> data,
			String outputFormat) throws IOException, EngineException;

}
