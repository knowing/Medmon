package de.lmu.ifi.dbs.medmon.medic.ui.selectionadapters;

import javax.persistence.EntityManager;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Therapy;
import de.lmu.ifi.dbs.medmon.database.model.TherapyResult;
import de.lmu.ifi.dbs.medmon.medic.core.service.EntityManagerProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;

public class SACreateTherapyResult extends SelectionAdapter {

	@Override
	public void widgetSelected(SelectionEvent e) {
		GlobalSelectionProvider selectionProvider = new GlobalSelectionProvider(Activator.getBundleContext());

		Therapy selectedTherapy = selectionProvider.getSelection(Therapy.class);
		if (selectedTherapy == null)
			return;
		
		/**
		 * Database Access Begin
		 */
		
		EntityManagerProvider emp = new EntityManagerProvider(Activator.getBundleContext());
		EntityManager entityManager = emp.getEntityManager();
		emp.unregister();

		entityManager.getTransaction().begin();

		TherapyResult mTherapyResult = new TherapyResult();
		Therapy mTherapy = selectedTherapy;
		entityManager.persist(mTherapyResult);

		// set some stuff
		mTherapyResult.setTherapy(mTherapy);
		mTherapy.getTherapyResults().add(mTherapyResult);

		entityManager.getTransaction().commit();

		/**
		 * Database Access End
		 */
		
		selectionProvider.updateSelection(Patient.class);
		selectionProvider.updateSelection(Therapy.class);
		selectionProvider.unregister();
	}
}
