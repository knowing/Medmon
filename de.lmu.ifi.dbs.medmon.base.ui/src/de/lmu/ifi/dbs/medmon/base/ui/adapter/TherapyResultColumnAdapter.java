package de.lmu.ifi.dbs.medmon.base.ui.adapter;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

import de.lmu.ifi.dbs.medmon.database.model.Therapy;
import de.lmu.ifi.dbs.medmon.database.model.TherapyResult;

public class TherapyResultColumnAdapter implements IWorkbenchAdapter {

	private static Object[] noChildren = new Object[]{};
	
	public Object[] getChildren(Object o) {
		return noChildren;
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	public String getLabel(Object o) {
		return "Result";
	}

	public Object getParent(Object o) {
		return ((TherapyResult)o).getTherapy();
	}

}