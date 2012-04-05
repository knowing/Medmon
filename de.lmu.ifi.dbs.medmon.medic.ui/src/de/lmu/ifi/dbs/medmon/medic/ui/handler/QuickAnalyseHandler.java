package de.lmu.ifi.dbs.medmon.medic.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import de.lmu.ifi.dbs.medmon.database.entity.Therapy;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.TherapyResultWizard;
import de.lmu.ifi.dbs.medmon.services.IGlobalSelectionService;

public class QuickAnalyseHandler extends AbstractHandler {

	public static final String ID = "de.lmu.ifi.dbs.medmon.medic.ui.quickanalyse";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IGlobalSelectionService selectionService = Activator.getGlobalSelectionService();
		Therapy selectedTherapy = selectionService.getSelection(Therapy.class);
		TherapyResultWizard wizard = new TherapyResultWizard(selectedTherapy);
		WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
		dialog.open();
		return null;
	}

}
