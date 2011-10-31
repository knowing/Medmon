package de.lmu.ifi.dbs.medmon.medic.core.service;


public class GlobalSelectionProvider implements IGlobalSelectionProvider {

	private IGlobalSelectionService	service = null;

	@Override
	public <T> void setSelection(Class<T> clazz, T selection) {
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
