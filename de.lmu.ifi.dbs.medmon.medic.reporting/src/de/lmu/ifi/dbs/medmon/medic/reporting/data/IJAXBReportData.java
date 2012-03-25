package de.lmu.ifi.dbs.medmon.medic.reporting.data;

import java.io.OutputStream;

/**
 * This interface will be replaced by POJOs and JavaScript
 * access inside the report.
 *
 * @author Stephan Picker
 * @version 0.1
 * @since 15.01.2012
 */
@Deprecated
public interface IJAXBReportData {

	/**
	 * Unique identifier for the data type.
	 * e.g. xrff
	 * @return unique data type id
	 */
	public String getId();

	/**
	 * 
	 * @param outputStream
	 */
	public void marshal(OutputStream outputStream);

}
