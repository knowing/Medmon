package de.lmu.ifi.dbs.medmon.database.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

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
public class Therapy implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	//bi-directional many-to-one association to Patient
    @ManyToOne
	@JoinColumn(name="PATIENT_ID", nullable=false )
	private Patient patient;

	@Temporal(TemporalType.DATE)
	Date therapyStart;

	@Temporal(TemporalType.DATE)
	Date therapyEnd;
	
	@Column
	String comment;

	@Column(/* 0-100 ? */)
	int success;

	@OneToMany(mappedBy="therapy")
	Set<TherapyResult> therapyResults;
	
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
	
	public void setTherapyResults(Set<TherapyResult> therapyResults) {
		this.therapyResults = therapyResults;
	}
	
	public Set<TherapyResult> getTherapyResults() {
		return therapyResults;
	}



}
