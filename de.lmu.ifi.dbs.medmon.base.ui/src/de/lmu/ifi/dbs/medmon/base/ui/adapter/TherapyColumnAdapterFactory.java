package de.lmu.ifi.dbs.medmon.base.ui.adapter;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;

import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.model.Therapy;
import de.lmu.ifi.dbs.medmon.database.model.TherapyResult;

public class TherapyColumnAdapterFactory implements IAdapterFactory {

	private static final Class[] types = new Class[] { IWorkbenchAdapter.class };
	
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adaptableObject instanceof Patient)
			return new PatientColumnAdapter();
		if(adaptableObject instanceof Therapy)
			return new TherapyColumnAdapter();
		if(adaptableObject instanceof TherapyResult)
			return new TherapyResultColumnAdapter();
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return types;
	}

}