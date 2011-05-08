package de.lmu.ifi.dbs.medmon.base.ui.adapter;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;

import de.lmu.ifi.dbs.knowing.core.graph.xml.DataProcessingUnit;

public class DPUAdapterFactory implements IAdapterFactory {

	private static final Class[] types = new Class[] { IWorkbenchAdapter.class, IWorkbenchColumnAdapter.class };
	
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adaptableObject instanceof DataProcessingUnit)
			return new DPUColumnAdapter();
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return types;
	}

}
