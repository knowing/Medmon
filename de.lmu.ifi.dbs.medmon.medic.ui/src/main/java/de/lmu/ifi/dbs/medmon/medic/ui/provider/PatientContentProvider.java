package de.lmu.ifi.dbs.medmon.medic.ui.provider;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;


public class PatientContentProvider implements IStructuredContentProvider {

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof Patient[])
			return (Patient[])inputElement;
		return getPatients();
	}
	
	private Patient[] getPatients() {
		EntityManager em = Activator.getEntityManagerService().createEntityManager();
		Query allPatients = em.createNamedQuery("Patient.findAll");
		List<Patient> patients = allPatients.getResultList();
		em.close();
		if(patients.isEmpty())
			return new Patient[0];
		return patients.toArray(new Patient[patients.size()]);
	}

}
