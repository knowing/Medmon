package de.lmu.ifi.dbs.medmon.medic.reporting.core;

import java.io.OutputStream;
import java.nio.file.Path;

public interface IReportData {

	public void marshal(Path file);
	
}
