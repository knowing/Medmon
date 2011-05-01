package de.lmu.ifi.dbs.medmon.base.ui.adapter;

import de.lmu.ifi.dbs.medmon.database.model.Patient;

public class PatientClusterAdapter {

	private final Patient patient;
	private final Object unit;
	
	public PatientClusterAdapter(Patient patient, Object unit) {
		this.patient = patient;
		this.unit = unit;
	}
	

	public Patient getPatient() {
		return patient;
	}
	
	public Object getCluster() {
		return unit;
	}

	public String getName() {
		return "NOT IMPLEMENTED";
	}

	public String getDescription() {
		return "NOT IMPLEMENTED";
	}
	
	public boolean isDefault() {
		return false;
	}
	
}
