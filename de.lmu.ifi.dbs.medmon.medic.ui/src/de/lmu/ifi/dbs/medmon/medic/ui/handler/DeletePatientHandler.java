package de.lmu.ifi.dbs.medmon.medic.ui.handler;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import de.lmu.ifi.dbs.medmon.base.ui.dialog.DialogFactory;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;

public class DeletePatientHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IGlobalSelectionProvider selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
		Patient selection = selectionProvider.getSelection(Patient.class);

		if (selection == null) {
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_INFORMATION | SWT.OK);
			messageBox.setText("Information");
			messageBox.setMessage("Bitte wählen sie zuerst einen Patienten aus!");
			messageBox.open();

			selection = DialogFactory.openPatientSelectionDialog(HandlerUtil.getActiveShell(event));

			if (selection != null) {
				messageBox = new MessageBox(new Shell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
				messageBox.setText("Patienten löschen?");
				messageBox.setMessage("Sind sie sicher, dass sie den ausgewählten Patienten löschen wollen ?!");
				int option = messageBox.open();

				if (option == SWT.YES) {
					try {
						Activator.getPatientService().deletePatient(selection);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		} else {
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
			messageBox.setText("Patienten löschen?");
			messageBox.setMessage("Sind sie sicher, dass sie den ausgewählten Patienten löschen wollen ?!");
			int option = messageBox.open();

			if (option == SWT.YES) {
				try {
					Activator.getPatientService().deletePatient(selection);
					selectionProvider.setSelection(Patient.class, null);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		selectionProvider.unregister();
		return null;
	}

}
