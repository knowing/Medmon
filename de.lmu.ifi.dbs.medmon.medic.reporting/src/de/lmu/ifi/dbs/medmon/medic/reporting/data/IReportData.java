package de.lmu.ifi.dbs.medmon.medic.reporting.data;

import java.io.OutputStream;
import java.nio.file.Path;

public interface IReportData {

	public void marshal(Path file);
	
}
