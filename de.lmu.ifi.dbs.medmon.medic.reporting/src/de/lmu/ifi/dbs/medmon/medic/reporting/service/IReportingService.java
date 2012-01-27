package de.lmu.ifi.dbs.medmon.medic.reporting.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.swt.browser.Browser;

import de.lmu.ifi.dbs.medmon.medic.reporting.core.BirtProcessingException;
import de.lmu.ifi.dbs.medmon.medic.reporting.data.IJAXBReportData;

public interface IReportingService {

	/**
	 * Renders a report to a File
	 * 
	 * @param reportId - this method will be using the files named [id].rptdesign, [id].xsd and [id].xml to render the report
	 * @param taskParameters - passes parameters to the reporting engine
	 * @param classLoader
	 * @param data - a list of IJAXBReportData interfaces, which will be passed to the reporting engine
	 * @return
	 * @throws IOException
	 * @throws EngineException
	 */
	public Path renderReport(String reportId, Map<String, Object> taskParameters, ClassLoader classLoader, List<IJAXBReportData> data) throws IOException, BirtProcessingException;

	/**
	 * Renders a report into a registered Browser
	 * Browsers can be registered via registerBrowser()
	 * 
	 * @param reportId - this method will be using the files named [id].rptdesign, [id].xsd and [id].xml to render the report
	 * @param id - id of a registered Browser
	 * @param taskParameters - passes parameters to the reporting engine
	 * @param classLoader
	 * @param data  - a list of IJAXBReportData interfaces, which will be passed to the reporting engine
	 * @throws EngineException
	 * @throws IOException
	 */
	public void renderReportToBrowser(String reportId, String id, Map<String, Object> taskParameters, ClassLoader classLoader, List<IJAXBReportData> data) throws BirtProcessingException, IOException;
	
	/**
	 * UNSUPPORTED YET
	 * 
	 * @param reportId
	 * @param destPath
	 * @param taskParameters
	 * @param classLoader
	 * @param data
	 */
	public void renderReportToPDF(String reportId, Path destPath, Map<String, Object> taskParameters, ClassLoader classLoader, List<IJAXBReportData> data);
	
	/**
	 * registers a Browser to the Service
	 * 
	 * @param browser - a Browser Object
	 * @param id - an id
	 */
	public void registerBrowser(Browser browser, String id);

	/**
	 * unregisters a Browser to the Service
	 * 
	 * @param id - an id
	 */
	public void unregisterBrowser(String id);
}
