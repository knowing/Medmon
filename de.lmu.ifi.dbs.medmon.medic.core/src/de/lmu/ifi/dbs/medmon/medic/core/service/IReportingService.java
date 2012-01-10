package de.lmu.ifi.dbs.medmon.medic.core.service;

import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

import de.lmu.ifi.dbs.medmon.medic.reporting.core.IReportData;

public interface IReportingService {

	public void renderReport(URL reportDesign, URL reportSchema, IReportData data, Map<String, Object> taskParameters, Path reportOutput, ClassLoader classLoader);

}
