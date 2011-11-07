package de.lmu.ifi.dbs.medmon.medic.ui.util;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.SectionPart;

import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;

public class DetailBlockSelectionAdapter {
	
	private IManagedForm managedForm;
	private SectionPart sectionPart;
	private IGlobalSelectionProvider selectionProvider;
	private Class[] clazzes;
	
	public DetailBlockSelectionAdapter(IManagedForm managedForm, SectionPart sectionPart ,IGlobalSelectionProvider selectionProvider, Class[] clazzes){
		this.managedForm = managedForm;
		this.sectionPart = sectionPart;
		this.selectionProvider = selectionProvider;
		this.clazzes = clazzes;
	}
	
	public void setSelection(ISelection selection){
		if(selection.isEmpty())
			return;
		managedForm.fireSelectionChanged(sectionPart, selection);
		Object object = ((IStructuredSelection)selection).getFirstElement();
		for(Class clazz : clazzes){
			if(object.getClass() == clazz){
				selectionProvider.setSelection(clazz, object);
				return;
			}
		}
	}
}
