package de.lmu.ifi.dbs.medmon.medic.ui.selectionadapters;

import java.util.Date;

import javax.persistence.EntityManager;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Therapy;
import de.lmu.ifi.dbs.medmon.medic.core.service.EntityManagerProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;

public class SACreateTherapy extends SelectionAdapter {

	@Override
	public void widgetSelected(SelectionEvent e) {
		GlobalSelectionProvider selectionProvider = new GlobalSelectionProvider(Activator.getBundleContext());

		Patient selectedPatient = selectionProvider.getSelection(Patient.class);
		if (selectedPatient == null)
			return;

		/**
		 * Database Access Begin
		 */

		EntityManagerProvider emp = new EntityManagerProvider(Activator.getBundleContext());
		EntityManager entityManager = emp.getEntityManager();
		emp.unregister();
		entityManager.getTransaction().begin();

		Patient mPatient = selectedPatient;
		Therapy mTherapy = new Therapy();

		entityManager.persist(mTherapy);

		mTherapy.setTherapyStart(new Date());
		mTherapy.setTherapyEnd(new Date());
		mTherapy.setPatient(mPatient);
		mPatient.getTherapies().add(mTherapy);

		entityManager.getTransaction().commit();

		/**
		 * Database Access End
		 */

		selectionProvider.updateSelection(Patient.class);
		selectionProvider.unregister();
	}
}
