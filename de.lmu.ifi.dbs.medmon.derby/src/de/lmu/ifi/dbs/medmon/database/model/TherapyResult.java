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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "THERAPYRESULT")
public class TherapyResult {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinColumn(name = "THERAPY_ID", nullable = false)
	private Therapy therapy;
	
	@Temporal(TemporalType.DATE)
	private Date timestamp;

	@Column
	private String comment;

	@Column
	private String caption;

	@Column
	private int success;

	@OneToOne(mappedBy = "therapyResult", cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	private Data data;
	
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

	public void setCaption(String report) {
		this.caption = report;
	}

	public String getCaption() {
		return caption;
	}

	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public void setSuccess(int success) {
		this.success = success;
		therapy.updateSuccess();
	}

	public int getSuccess() {
		return success;
	}

	public Data getData() {
		return data;
	}
	
	public void setData(Data data) {
		this.data = data;
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
