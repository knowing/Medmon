package de.lmu.ifi.dbs.medmon.medic.ui.handler;

import java.io.IOException;

import javax.persistence.EntityManager;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import de.lmu.ifi.dbs.medmon.base.ui.dialog.DialogFactory;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;

/**
 * 
 * @author Stephan Picker, Nepomuk Seiler
 * @version 0.1
 * @since 11.2012
 */
public class DeletePatientHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IGlobalSelectionProvider selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
		Patient selection = selectionProvider.getSelection(Patient.class);
		Shell shell = HandlerUtil.getActiveWorkbenchWindow(event).getShell();

		if (selection == null) {
			MessageDialog.openInformation(shell, "Information", "Bitte w�hlen sie zuerst einen Patienten aus!");
			selection = DialogFactory.openPatientSelectionDialog(HandlerUtil.getActiveShell(event));
		}
		
		boolean option = MessageDialog.openConfirm(shell, "Patienten l�schen?",
				"Sind sie sicher, dass sie den ausgew�hlten Patienten l�schen wollen ?");

		if (option) {
			try {
				deletePatient(selection);
				selectionProvider.setSelection(Patient.class, null);
			} catch (Exception e) {
				MessageDialog.openError(shell, "Fehler beim loeschen des Patienten", e.getMessage());
				e.printStackTrace();
			}
		}

		selectionProvider.unregister();
		return null;
	}

	private void deletePatient(Patient patient) throws IOException {
		EntityManager workerEM = Activator.getEntityManagerService().createEntityManager();
		workerEM.getTransaction().begin();
		Patient mPatient = workerEM.find(Patient.class, patient.getId());
		workerEM.remove(mPatient);
		workerEM.getTransaction().commit();
		workerEM.close();
//		Activator.getPatientService().releasePatient(patient);
	}

}
