package de.lmu.ifi.dbs.medmon.base.ui.adapter;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

import de.lmu.ifi.dbs.medmon.database.entity.Patient;

public class PatientColumnAdapter implements IAdapterFactory {

	private static final Class[] types = new Class[] { IWorkbenchColumnAdapter.class, IWorkbenchAdapter.class };
	
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adapterType.equals(IWorkbenchAdapter.class)  && adaptableObject instanceof Patient ) 
			return new PatientColumnAdapterImpl();
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return types;
	}
	
	public class PatientColumnAdapterImpl implements IWorkbenchAdapter {
		public Object[] getChildren(Object o) {
			return ((Patient)o).getTherapies().toArray();
		}

		public ImageDescriptor getImageDescriptor(Object object) {
			return null;
		}

		public String getLabel(Object o) {
			Patient p = (Patient)o;
			return p.getFirstname() + ", " + p.getLastname();
		}

		public Object getParent(Object o) {
			return null;
		}
		
	}

}