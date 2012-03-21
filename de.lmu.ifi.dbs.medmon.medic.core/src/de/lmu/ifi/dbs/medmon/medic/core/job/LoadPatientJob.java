package de.lmu.ifi.dbs.medmon.medic.core.job;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;

public class LoadPatientJob extends Job {

	public LoadPatientJob() {
		super("Load patient");
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		return Status.OK_STATUS;
	}
	
	public static Patient getPatient() {
		return null;
	}
	
	public static Patient[] getPatients() {
		EntityManager em = JPAUtil.createEntityManager();
		em.getTransaction().begin();
		Query allPatients = em.createNamedQuery("Patient.findAll");
		List<Patient> patients = allPatients.getResultList();
		em.getTransaction().commit();
		em.close();
		return patients.toArray(new Patient[patients.size()]);
	}

}
