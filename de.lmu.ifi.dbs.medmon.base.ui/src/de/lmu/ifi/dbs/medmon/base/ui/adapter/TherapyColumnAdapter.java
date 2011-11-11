package de.lmu.ifi.dbs.medmon.base.ui.adapter;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

import de.lmu.ifi.dbs.medmon.database.model.Therapy;

public class TherapyColumnAdapter implements IWorkbenchAdapter {
	
	public Object[] getChildren(Object o) {
		return ((Therapy)o).getTherapyResults().toArray();
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	public String getLabel(Object o) {
		return "Therapy";
	}

	public Object getParent(Object o) {
		return ((Therapy)o).getPatient();
	}

}