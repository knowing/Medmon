package de.lmu.ifi.dbs.medmon.base.ui.filter;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.sapphire.modeling.ModelElementList;

import de.lmu.ifi.dbs.knowing.core.model.IDataProcessingUnit;
import de.lmu.ifi.dbs.knowing.core.model.INode;

/**
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 08.05.2011
 *
 */
public class DPUInputFilter extends ViewerFilter {

	private String[] loaderIds = new String[0];
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if(!(element instanceof IDataProcessingUnit))
			return false;
		IDataProcessingUnit dpu = (IDataProcessingUnit) element;
		ModelElementList<INode> nodes = dpu.getNodes();
		Set<String> dpuLoader = new HashSet<String>();
		for (INode node : nodes) {
			String type = node.getType().getText();
			if(type != null && type.equals("loader")) {
				String id = node.getFactoryId().getText();
				dpuLoader.add(id);
			}
		}
		
		for(String id : loaderIds) 
			dpuLoader.remove(id);
			
		
		return dpuLoader.isEmpty();
	}
	
	/**
	 * @param loaderIds the loaderIds to set
	 */
	public void setLoaderIds(String[] loaderIds) {
		this.loaderIds = loaderIds;
	}

}
