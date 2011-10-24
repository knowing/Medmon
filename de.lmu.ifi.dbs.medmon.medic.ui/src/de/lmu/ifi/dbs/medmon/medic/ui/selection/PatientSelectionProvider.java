package de.lmu.ifi.dbs.medmon.medic.ui.selection;

import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionService;

public class PatientSelectionProvider implements IGlobalSelectionProvider<Patient> {

	private IGlobalSelectionService	service;

	@Override
	public void setSelection(Patient selection) {
		service.setSelection(Patient.class, selection);
	}

	@Override
	public void setGlobalSelectionService(IGlobalSelectionService service) {
		this.service = service;
	}
}
