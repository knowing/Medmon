package de.lmu.ifi.dbs.medmon.medic.reporting.service;

import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

import org.eclipse.swt.browser.Browser;

public interface IReportingService {

	public void renderReport(URL reportDesign, URL reportSchema, Map<String, Object> taskParameters, Path reportOutput, ClassLoader classLoader);
	
	public void displayReport(Browser browser);

}
