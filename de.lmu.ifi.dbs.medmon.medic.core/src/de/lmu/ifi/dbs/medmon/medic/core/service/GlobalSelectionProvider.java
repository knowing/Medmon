package de.lmu.ifi.dbs.medmon.medic.core.service;

import org.osgi.framework.ServiceRegistration;

import de.lmu.ifi.dbs.medmon.medic.core.Activator;

public class GlobalSelectionProvider implements IGlobalSelectionProvider {

	private IGlobalSelectionService service = null;
	private ServiceRegistration<IGlobalSelectionProvider> registerService = null;

	/**
	 * create a new GlobalSelectionProvider service
	 * 
	 * this service will be registered automatically. when finished working with
	 * the service call close().
	 */
	public GlobalSelectionProvider() {
		registerService = Activator.getBundleContext().registerService(IGlobalSelectionProvider.class, this, null);
	}

	/**
	 * the service will be unregistered
	 */
	public void unregister() {
		if (registerService == null)
			return;
		registerService.unregister();
	}

	@Override
	public <T> void setSelection(Class<T> clazz, T selection) {
		if (service != null)
			service.setSelection(clazz, selection);
		else
			throw new NullPointerException("GlobalSelectionProvider.setSelection() -> USE setGlobalSelectionService()");
	}

	public <T> T getSelection(Class<T> clazz) {
		return service.getSelection(clazz);
	}

	@Override
	public void setGlobalSelectionService(IGlobalSelectionService service) {
		if (service != null)
			this.service = service;
		else
			throw new IllegalArgumentException("GlobalSelectionProvider.setGlobalSelectionService() -> PARAM MUST NOT NULL");
	}
}
