package de.lmu.ifi.dbs.medmon.medic.ui.wizard;

import java.io.IOException;

import javax.persistence.EntityManager;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.CreatePatientPage;

public class CreatePatientWizard extends Wizard implements IWorkbenchWizard, IExecutableExtension {

	/* Pages */
	private CreatePatientPage	patientpage;

	private String				finalPerspectiveId;

	public CreatePatientWizard() {
		setWindowTitle("Patient erstellen");
	}

	@Override
	public void addPages() {
		addPage(patientpage = new CreatePatientPage());
	}

	@Override
	public boolean performFinish() {
		Patient patient = null;
		try {
			patient = Activator.getPatientService().createPatient();
		} catch (IOException e1) {
			e1.printStackTrace();
			MessageDialog.openError(getShell(), "Patient konnte nicht erstellt werden", e1.getMessage());
			return false;
		}

		EntityManager tempEM = JPAUtil.createEntityManager();
		tempEM.getTransaction().begin();
		Patient mPatient = tempEM.find(Patient.class, patient.getId());
		patientpage.initializePatient(mPatient);
		tempEM.getTransaction().commit();
		tempEM.close();

		IGlobalSelectionProvider SelectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
		SelectionProvider.setSelection(Patient.class, patient);
		SelectionProvider.unregister();

		if (finalPerspectiveId != null && !finalPerspectiveId.isEmpty()) {
			try {
				PlatformUI.getWorkbench().showPerspective(finalPerspectiveId, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
			} catch (WorkbenchException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {

	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		finalPerspectiveId = config.getAttribute("finalPerspective"); //$NON-NLS-1$		
	}

}
