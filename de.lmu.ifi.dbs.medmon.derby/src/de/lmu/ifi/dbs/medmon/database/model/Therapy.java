package de.lmu.ifi.dbs.medmon.database.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "THERAPY")
@NamedQueries({})
public class Therapy {

	@Column
	Date start;

	@Column
	Date end;

	@Column
	String comment;

	@Column(/*0-100 ?*/)
	int success;
	
	//class TherapyResults doesn't exist yet
	@OneToMany(mappedBy="therapy")
	Set<Object> therapyResults;

}
