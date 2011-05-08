package de.lmu.ifi.dbs.medmon.base.ui.filter;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import de.lmu.ifi.dbs.knowing.core.graph.xml.*;
import de.lmu.ifi.dbs.knowing.core.graph.*;

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
		if(!(element instanceof DataProcessingUnit))
			return false;
		DataProcessingUnit dpu = (DataProcessingUnit) element;
		Node[] nodes = dpu.nodes();
		Set<String> dpuLoader = new HashSet<String>();
		for (Node node : nodes) {
			String type = node.nodeType();
			if(type.equals("loader")) {
				String id = node.factoryId();
				dpuLoader.add(id);
				System.out.println("Id: " + id);
			}
		}
		for(String id : loaderIds)  {
			System.out.println("Remove id: " + id);
			dpuLoader.remove(id);
		}
			
		
		return dpuLoader.isEmpty();
	}
	
	/**
	 * @param loaderIds the loaderIds to set
	 */
	public void setLoaderIds(String[] loaderIds) {
		this.loaderIds = loaderIds;
	}

}
