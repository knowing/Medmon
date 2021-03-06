package de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.ManagedForm;

import de.lmu.ifi.dbs.medmon.medic.ui.pages.DPUMasterBlock;

public class SelectDPUPage extends WizardPage {

	private ManagedForm managedForm;

	/**
	 * Create the wizard.
	 */
	public SelectDPUPage() {
		super("SelectDPUPage");
		setTitle("Analyseverfahren ausw\u00e4hlen.");
		setDescription("W\u00e4hlen Sie ein Verfahren aus um die Daten zu analysieren");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		managedForm = createManagedForm(parent);
		DPUMasterBlock block = new DPUMasterBlock();
		block.createContent(managedForm);
		setControl(managedForm.getForm());
		block.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete(!event.getSelection().isEmpty());
			}
		});
		setPageComplete(true);
	}
	
	protected ManagedForm createManagedForm(final Composite parent) {
		ManagedForm managedForm = new ManagedForm(parent);
		managedForm.setContainer(this);
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = SWT.FILL;
		managedForm.getForm().setLayoutData(gridData);
		return managedForm;
	}
	
	@Override
	public void dispose() {
		managedForm.dispose();
		super.dispose();
	}
	

}
