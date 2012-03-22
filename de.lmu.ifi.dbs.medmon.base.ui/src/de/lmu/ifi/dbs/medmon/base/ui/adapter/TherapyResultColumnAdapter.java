package de.lmu.ifi.dbs.medmon.base.ui.adapter;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

import de.lmu.ifi.dbs.medmon.database.entity.TherapyResult;

/**
 *
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 2011
 */
public class TherapyResultColumnAdapter implements IAdapterFactory {

	private static final Class[] types = new Class[] { IWorkbenchAdapter.class };
	
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adapterType.equals(IWorkbenchAdapter.class) && adaptableObject instanceof TherapyResult)
			return new TherapyResultColumnAdapterImpl();
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return types;
	}

	public class TherapyResultColumnAdapterImpl implements IWorkbenchAdapter {

		public Object[] getChildren(Object o) {
			return new Object[]{};
		}

		public ImageDescriptor getImageDescriptor(Object object) {
			return null;
		}

		public String getLabel(Object o) {
			return ((TherapyResult) o).getCaption();
		}

		public Object getParent(Object o) {
			return ((TherapyResult) o).getTherapy();
		}

	}

}