package de.lmu.ifi.dbs.medmon.base.ui.adapter;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.base.ui.Activator;

public class SensorAdapterFactory implements IAdapterFactory {

	private static final Class[] types = new Class[] { IWorkbenchColumnAdapter.class, IWorkbenchAdapter.class };
	private static final Logger log = LoggerFactory.getLogger(Activator.PLUGIN_ID);
	
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		log.debug("if(!(adaptableObject instanceof SensorAdapter))");
		log.debug("    return null;");
		
		if(adapterType.equals(IWorkbenchColumnAdapter.class)) 
			return new SensorColumnAdapter();
		else if(adapterType.equals(IWorkbenchAdapter.class)) 
			return new SensorColumnAdapter();
		
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return types;
	}

}
