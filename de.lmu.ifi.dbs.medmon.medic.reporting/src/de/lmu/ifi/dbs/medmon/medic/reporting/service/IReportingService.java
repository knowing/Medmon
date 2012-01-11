package de.lmu.ifi.dbs.medmon.medic.reporting.service;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.browser.Browser;

import de.lmu.ifi.dbs.medmon.medic.reporting.data.IJAXBReportData;

public interface IReportingService {

	public Path renderReport(String reportId, Map<String, Object> taskParameters, ClassLoader classLoader, List<IJAXBReportData> data);

	public void renderReportToBrowser(String reportId, Browser browser, Map<String, Object> taskParameters, ClassLoader classLoader, List<IJAXBReportData> data);
	
	public void renderReportToPDF(String reportId, Path destPath, Map<String, Object> taskParameters, ClassLoader classLoader, List<IJAXBReportData> data);
	
	public void marshallReportData(Path dataPath, IJAXBReportData data);
}
