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

public class SADeleteTherapyResult extends SelectionAdapter {

	@Override
	public void widgetSelected(SelectionEvent e) {
		GlobalSelectionProvider selectionProvider = new GlobalSelectionProvider(Activator.getBundleContext());

		TherapyResult selectedTherapyResult = selectionProvider.getSelection(TherapyResult.class);

		if (selectedTherapyResult == null) {
			return;
		}

		/**
		 * Database Access Begin
		 */

		EntityManagerProvider emp = new EntityManagerProvider(Activator.getBundleContext());
		EntityManager entityManager = emp.getEntityManager();
		emp.unregister();

		selectedTherapyResult.getTherapy().getTherapyResults().remove(selectedTherapyResult);

		entityManager.getTransaction().begin();
		entityManager.remove(selectedTherapyResult);
		entityManager.getTransaction().commit();

		/**
		 * Database Access End
		 */

		selectionProvider.updateSelection(Patient.class);
		selectionProvider.updateSelection(Therapy.class);
		selectionProvider.setSelection(TherapyResult.class, null);
		selectionProvider.unregister();
	}
}
