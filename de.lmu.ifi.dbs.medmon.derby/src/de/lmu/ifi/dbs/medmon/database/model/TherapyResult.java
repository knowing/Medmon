package de.lmu.ifi.dbs.medmon.database.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
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

	@ManyToOne
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

}
