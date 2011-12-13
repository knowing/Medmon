package de.lmu.ifi.dbs.medmon.database.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "THERAPY")
@NamedQueries({ @NamedQuery(name = "Therapy.findByPatientId", query = "SELECT t FROM Therapy t WHERE t.patient = :patientId") })
public class Therapy implements Serializable {
	private static final long	serialVersionUID	= 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column
	String caption;

	// bi-directional many-to-one association to Patient
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinColumn(name = "PATIENT_ID", nullable = false)
	private Patient patient;

	@OneToMany(mappedBy = "therapy", cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	private Set<TherapyResult>	therapyResults = new HashSet<TherapyResult>();

	@Temporal(TemporalType.DATE)
	private Date therapyStart;

	@Temporal(TemporalType.DATE)
	private Date therapyEnd;

	@Column
	private String comment;

	@Column(/* 0-100 ? */)
	private int success;

	public void updateSuccess() {
		if (!therapyResults.isEmpty()) {
			int sum = 0;
			for (TherapyResult therapyResult : therapyResults) {
				sum += therapyResult.getSuccess();
			}
			sum /= therapyResults.size();
			setSuccess(sum);
		} else {
			setSuccess(0);
		}
	}

	/**
	 * Getters and Setters
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setTherapyStart(Date start) {
		this.therapyStart = start;
	}

	public Date getTherapyStart() {
		return therapyStart;
	}

	public void setTherapyEnd(Date end) {
		this.therapyEnd = end;
	}

	public Date getTherapyEnd() {
		return therapyEnd;
	}

	public void setSuccess(int success) {
		this.success = success;
	}

	public int getSuccess() {
		return success;
	}

	public Set<TherapyResult> getTherapyResults() {
		return therapyResults;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String name) {
		this.caption = name;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getId() {
		return id;
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
		Therapy other = (Therapy) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
