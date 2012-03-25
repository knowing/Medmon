package de.lmu.ifi.dbs.medmon.database.entity;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

/**
 * 
 * Currently not an entity.
 * 
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 25.03.2012
 */
public class Report {

	private long	id;

	private String	reportId;

	private String	file;

	private String	format;

	private Date	timestamp;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getReportId() {
		return reportId;
	}

	public void setReportId(String reportId) {
		this.reportId = reportId;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getFormat() {
		return format;
	}

	/**
	 * Allowed values "html" and "pdf"
	 * @param format
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public Path toPath() {
		return Paths.get(file);
	}

}
