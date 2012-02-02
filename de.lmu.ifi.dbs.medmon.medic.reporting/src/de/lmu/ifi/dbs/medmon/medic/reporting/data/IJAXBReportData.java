package de.lmu.ifi.dbs.medmon.medic.reporting.data;

import java.io.OutputStream;
import java.nio.file.Path;

public interface IJAXBReportData {

	public String getId();

	public void marshal(OutputStream outputStream);

}
