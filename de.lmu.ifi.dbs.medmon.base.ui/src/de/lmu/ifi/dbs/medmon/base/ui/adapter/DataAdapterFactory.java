package de.lmu.ifi.dbs.medmon.base.ui.adapter;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;

import de.lmu.ifi.dbs.medmon.database.model.Data;

public class DataAdapterFactory implements IAdapterFactory {

	private static final Class[] types = new Class[] { IWorkbenchColumnAdapter.class, IWorkbenchAdapter.class };
	
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(!(adaptableObject instanceof Data))
			return null;
		return new DataColumnAdapter();
	}

	@Override
	public Class[] getAdapterList() {
		return types;
	}

}
