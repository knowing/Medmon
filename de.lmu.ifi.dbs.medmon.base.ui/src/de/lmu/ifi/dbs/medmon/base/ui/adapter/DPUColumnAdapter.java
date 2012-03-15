package de.lmu.ifi.dbs.medmon.base.ui.adapter;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;

import de.lmu.ifi.dbs.knowing.core.model.IDataProcessingUnit;
import de.lmu.ifi.dbs.medmon.base.ui.util.ResourceManager;
import de.lmu.ifi.dbs.medmon.medic.core.util.IMedmonConstants;

/**
 * @author Nepomuk Seiler
 * @version 0.2
 * @since 08.05.2011
 * 
 */

public class DPUColumnAdapter implements IAdapterFactory {

	private static final Class[] types = new Class[] { IWorkbenchAdapter.class, IWorkbenchColumnAdapter.class };

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(!(adaptableObject instanceof IDataProcessingUnit))
			return null;
		if(adapterType.equals(IWorkbenchAdapter.class))
			return new DPUColumnAdapterImpl();
		if(adapterType.equals(IWorkbenchColumnAdapter.class))
			return new DPUColumnAdapterImpl();
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return types;
	}
	
	public class DPUColumnAdapterImpl implements IWorkbenchColumnAdapter {

		@Override
		public String getColumnText(Object element, int columnIndex) {
			IDataProcessingUnit dpu = (IDataProcessingUnit) element;
			switch (columnIndex) {
			case 0:
				return dpu.getName().getText();
			case 1:
				return dpu.getDescription().getText();
			case 2:
				return dpu.getTags().getText();
			default:
				return getLabel(dpu);
			}
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return ResourceManager.getPluginImage(IMedmonConstants.BASE_UI_PLUGIN, IMedmonConstants.IMG_CHECKED_16);
			default:
				return null;
			}
		}

		@Override
		public String getLabel(Object o) {
			return "-";
		}

		@Override
		public ImageDescriptor getImageDescriptor(Object object) {
			return null;
		}

		@Override
		public Object[] getChildren(Object o) {
			return null;
		}

		@Override
		public Object getParent(Object o) {
			return null;
		}
	}
}
