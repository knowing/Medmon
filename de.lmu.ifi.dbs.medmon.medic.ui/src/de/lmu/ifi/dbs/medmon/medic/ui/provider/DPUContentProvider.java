package de.lmu.ifi.dbs.medmon.medic.ui.provider;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.lmu.ifi.dbs.knowing.core.graph.xml.DataProcessingUnit;
import de.lmu.ifi.dbs.knowing.core.graph.xml.PersistentNode;


/**
 * This provider structures the DPU in categories and gives access to the
 * parameters via TreeElements
 * @author Nepomuk Seiler
 * @version 0.1
 */
public class DPUContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof DataProcessingUnit[])
			return (DataProcessingUnit[])inputElement;
		return new Object[0];
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if(!(parentElement instanceof DataProcessingUnit))
			return new Object[0];
		DataProcessingUnit dpu = (DataProcessingUnit) parentElement;
		List<PersistentNode> nodes = dpu.getNodes();
		return (PersistentNode[]) nodes.toArray(new PersistentNode[nodes.size()]);
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if(element instanceof DataProcessingUnit)
			return !((DataProcessingUnit)element).getNodes().isEmpty();
		return false;
	}
	
	@Override
	public void dispose() {
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	
	}

}
