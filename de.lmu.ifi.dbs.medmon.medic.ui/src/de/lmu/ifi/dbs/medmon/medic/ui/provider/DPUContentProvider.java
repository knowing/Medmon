package de.lmu.ifi.dbs.medmon.medic.ui.provider;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.lmu.ifi.dbs.knowing.core.model.IDataProcessingUnit;
import de.lmu.ifi.dbs.knowing.core.model.INode;


/**
 * This provider structures the DPU in categories and gives access to the
 * parameters via TreeElements
 * @author Nepomuk Seiler
 * @version 0.1
 */
public class DPUContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof IDataProcessingUnit[])
			return (IDataProcessingUnit[])inputElement;
		return new Object[0];
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if(!(parentElement instanceof IDataProcessingUnit))
			return new Object[0];
		IDataProcessingUnit dpu = (IDataProcessingUnit) parentElement;
		return (INode[]) dpu.getNodes().toArray(new INode[dpu.getNodes().size()]);
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
//		if(element instanceof DataProcessingUnit)
//			return !((DataProcessingUnit)element).getNodes().isEmpty();
		return false;
	}
	
	@Override
	public void dispose() {
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	
	}

}
