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

import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages.CreatePatientPage;

/**
 * 
 *
 * @author Stephan Picker, Nepomuk Seiler
 * @version 0.1
 * @since 15.03.2012
 */
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
		try {
			EntityManager tempEM = Activator.getEntityManagerService().createEntityManager();
			tempEM.getTransaction().begin();
			Patient mPatient = new Patient();
			patientpage.initializePatient(mPatient);
			tempEM.persist(mPatient);
			tempEM.getTransaction().commit();
			tempEM.close();
			
			Activator.getPatientService().initializePatient(mPatient);
			IGlobalSelectionProvider SelectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
			SelectionProvider.setSelection(Patient.class, mPatient);
			SelectionProvider.unregister();

		} catch (IOException e) {
			e.printStackTrace();
			MessageDialog.openError(getShell(), "Patient konnte nicht erstellt werden", e.getMessage());
			return false;
		}



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
