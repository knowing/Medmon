package de.lmu.ifi.dbs.medmon.medic.reporting.service;

import java.io.IOException;
import java.util.List;

import org.eclipse.birt.report.engine.api.EngineException;

import de.lmu.ifi.dbs.medmon.database.entity.Report;
import de.lmu.ifi.dbs.medmon.medic.reporting.core.BirtProcessingException;
import de.lmu.ifi.dbs.medmon.medic.reporting.data.IJAXBReportData;

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
	 * @param data
	 *            - a list of IJAXBReportData interfaces, which will be passed
	 *            to the reporting engine
	 * @return
	 * @throws IOException
	 * @throws EngineException
	 */
	public Report renderReport(String reportId, ClassLoader classLoader, List<IJAXBReportData> data,
			String outputFormat) throws IOException, BirtProcessingException;

}
