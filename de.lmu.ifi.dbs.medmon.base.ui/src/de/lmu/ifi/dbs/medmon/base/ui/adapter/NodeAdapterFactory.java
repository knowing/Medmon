package de.lmu.ifi.dbs.medmon.base.ui.adapter;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

import de.lmu.ifi.dbs.knowing.core.model.INode;
import de.lmu.ifi.dbs.medmon.base.ui.util.ResourceManager;
import de.lmu.ifi.dbs.medmon.medic.core.util.IMedmonConstants;

/**
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 09.05.2011
 *
 */
public class NodeAdapterFactory implements IAdapterFactory {

	private static final Class[] types = new Class[] { IWorkbenchAdapter.class };
	
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(!(adaptableObject instanceof INode)) 
			return null;
			
		return new IWorkbenchAdapter() {
			
			@Override
			public Object getParent(Object o) {
				return null;
			}
			
			@Override
			public String getLabel(Object o) {
				return ((INode)o).getId().getText();
			}
			
			@Override
			public ImageDescriptor getImageDescriptor(Object object) {
				return ResourceManager.getPluginImageDescriptor(IMedmonConstants.BASE_UI_PLUGIN, IMedmonConstants.IMG_IMPORT_16);
			}
			
			@Override
			public Object[] getChildren(Object o) {
				return null;
			}
		};
	}

	@Override
	public Class[] getAdapterList() {
		return types;
	}

}
