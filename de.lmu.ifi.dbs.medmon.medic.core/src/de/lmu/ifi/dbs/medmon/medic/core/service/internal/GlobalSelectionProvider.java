package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionService;

public class GlobalSelectionProvider<T> implements IGlobalSelectionProvider<T> {

	private IGlobalSelectionService	service = null;

	@Override
	public void setSelection(Class<T> clazz, T selection) {
		if(service != null)
			service.setSelection(clazz, selection);
		else
			throw new NullPointerException("GlobalSelectionProvider.setSelection() -> USE setGlobalSelectionService()");
	}

	@Override
	public void setGlobalSelectionService(IGlobalSelectionService service) {
		if (service != null)
			this.service = service;
		else
			throw new IllegalArgumentException("GlobalSelectionProvider.setGlobalSelectionService() -> PARAM MUST NOT NULL");
	}

}
