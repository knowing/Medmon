package de.lmu.ifi.dbs.medmon.base.ui.adapter;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import de.lmu.ifi.dbs.knowing.core.graph.xml.DataProcessingUnit;
import de.lmu.ifi.dbs.medmon.base.ui.util.ResourceManager;
import de.lmu.ifi.dbs.medmon.medic.core.util.IMedmonConstants;

/**
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 08.05.2011
 *
 */
public class DPUColumnAdapter implements IWorkbenchColumnAdapter {


	@Override
	public String getColumnText(Object element, int columnIndex) {
		DataProcessingUnit dpu = (DataProcessingUnit)element;
		switch(columnIndex) {
		case 0: return dpu.name();
		case 1: return dpu.description();
		case 2: return dpu.tags();
		default: return getLabel(dpu);
		}
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		switch(columnIndex) {
		case 0: return ResourceManager.getPluginImage(IMedmonConstants.BASE_UI_PLUGIN, IMedmonConstants.IMG_CHECKED_16);
		default: return null;
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
