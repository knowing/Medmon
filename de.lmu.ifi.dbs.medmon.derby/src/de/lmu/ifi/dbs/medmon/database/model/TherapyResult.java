package de.lmu.ifi.dbs.medmon.database.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "THERAPYRESULT")
public class TherapyResult {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
	@JoinColumn(name = "THERAPY_ID", nullable = false)
	private Therapy therapy;
	
	@Temporal(TemporalType.DATE)
	Date timestamp;

	@Column
	String comment;

	@Column
	String report;

	@Column
	int success;

	public void setTherapy(Therapy therapy) {
		this.therapy = therapy;
	}

	public Therapy getTherapy() {
		return therapy;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setReport(String report) {
		this.report = report;
	}

	public String getReport() {
		return report;
	}

	public void setSuccess(int success) {
		this.success = success;
		therapy.updateSuccess();
	}

	public int getSuccess() {
		return success;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TherapyResult other = (TherapyResult) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
