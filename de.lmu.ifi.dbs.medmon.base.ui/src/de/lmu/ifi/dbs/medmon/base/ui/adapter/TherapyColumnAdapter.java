package de.lmu.ifi.dbs.medmon.base.ui.adapter;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

import de.lmu.ifi.dbs.medmon.database.entity.Therapy;

public class TherapyColumnAdapter implements IAdapterFactory {

	private static final Class[] types = new Class[] { IWorkbenchAdapter.class };
	
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adapterType.equals(IWorkbenchAdapter.class) && adaptableObject instanceof Therapy)
			return new TherapyColumnAdapterImpl();
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return types;
	}
	
	public class TherapyColumnAdapterImpl implements IWorkbenchAdapter {
		
		public Object[] getChildren(Object o) {
			return ((Therapy)o).getTherapyResults().toArray();
		}

		public ImageDescriptor getImageDescriptor(Object object) {
			return null;
		}

		public String getLabel(Object o) {
			Therapy t = (Therapy)o;
			if(t.getCaption() == null || t.getCaption().isEmpty())
				return "<Namenlose Therapie>";
			return t.getCaption();
		}

		public Object getParent(Object o) {
			return ((Therapy)o).getPatient();
		}

	}

}