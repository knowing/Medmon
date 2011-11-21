package de.lmu.ifi.dbs.medmon.medic.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.handlers.HandlerUtil;

import de.lmu.ifi.dbs.medmon.base.ui.dialog.DialogFactory;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;

public class OpenPatientHandler extends AbstractHandler implements IHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Patient patient = DialogFactory.openPatientSelectionDialog(HandlerUtil.getActiveShell(event));
		if (patient == null) {
			return null;
		}
		IGlobalSelectionProvider selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
		selectionProvider.setSelection(Patient.class, patient);
		selectionProvider.unregister();
		return null;
	}
}
