package de.lmu.ifi.dbs.medmon.base.ui.filter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import de.lmu.ifi.dbs.knowing.core.model.IDataProcessingUnit;

/**
 * 
 * @author Nepomuk Seiler
 * @version 0.2
 * @since 08.05.2011
 *
 */
public class DPUSearchFilter extends ViewerFilter {

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
		
		//No content means nothing matches
		String content = dpu.getTags().getContent();
		if(content == null)
			return false;
		
		//Case-insensitive
		String[] tags = content.split(",");
		for (String tag : tags) {
			if(tag.toLowerCase().matches(searchString.toLowerCase()))
				return true;
		}
		return dpu.getName().getText().matches(searchString);
	}

}
