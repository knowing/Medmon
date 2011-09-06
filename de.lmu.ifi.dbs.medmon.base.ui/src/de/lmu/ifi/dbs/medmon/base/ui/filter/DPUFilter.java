package de.lmu.ifi.dbs.medmon.base.ui.filter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import de.lmu.ifi.dbs.knowing.core.model.IDataProcessingUnit;


public class DPUFilter extends ViewerFilter {

	private String searchString;

	public void setSearchText(String s) {
		// Search must be a substring of the existing value
		this.searchString = ".*" + s + ".*";
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if(!(element instanceof IDataProcessingUnit))
			return false;
		IDataProcessingUnit dpu = (IDataProcessingUnit) element;
		if(searchString == null || searchString.isEmpty())
			return true;
		String[] tags = dpu.getTags().getContent().split(",");
		for (String tag : tags) {
			if(tag.matches(searchString))
				return true;
		}
		return dpu.getName().getText().matches(searchString);
	}

}
