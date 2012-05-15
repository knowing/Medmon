package de.lmu.ifi.dbs.medmon.base.ui.dialog;

import java.util.List;

import javax.persistence.EntityManager;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import de.lmu.ifi.dbs.medmon.base.ui.Activator;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;

public class DialogFactory {

	public static Patient openPatientSelectionDialog(Shell parent) {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(parent, new LabelProvider());
		dialog.setElements(loadPatients());
		dialog.open();
		return (Patient) dialog.getFirstResult();
	}

	private static Patient[] loadPatients() {
		EntityManager em = Activator.getEntityManagerService().createEntityManager();

		
		List<Patient> patients = em.createNamedQuery("Patient.findAll").getResultList();
		em.close();

		return patients.toArray(new Patient[patients.size()]);
	}
}
