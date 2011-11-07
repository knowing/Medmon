package de.lmu.ifi.dbs.medmon.base.ui.adapter;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

import de.lmu.ifi.dbs.medmon.database.model.Patient;

public class PatientColumnAdapter implements IWorkbenchAdapter {

	public Object[] getChildren(Object o) {
		return ((Patient)o).getTherapies().toArray();
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	public String getLabel(Object o) {
		return "Patient";
	}

	public Object getParent(Object o) {
		return null;
	}

}