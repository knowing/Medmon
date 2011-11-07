package de.lmu.ifi.dbs.medmon.medic.core.service;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import de.lmu.ifi.dbs.medmon.medic.core.Activator;

public class GlobalSelectionProvider implements IGlobalSelectionProvider {

	private BundleContext bundleContext = null;
	private IGlobalSelectionService service = null;
	private ServiceRegistration<IGlobalSelectionProvider> registerService = null;
	private Map<IGlobalSelectionListener, ServiceRegistration<IGlobalSelectionListener>> registrationMap = new HashMap<IGlobalSelectionListener, ServiceRegistration<IGlobalSelectionListener>>();

	/**
	 * create a new GlobalSelectionProvider service
	 * 
	 * this service will be registered automatically. when finished working with
	 * the service call unregister().
	 */
	public GlobalSelectionProvider(BundleContext bundleContext) {
		if (bundleContext == null)
			throw new NullPointerException("GlobalSelectionProvider() => bundleContext mustnot be null");
		this.bundleContext = bundleContext;
		registerService = bundleContext.registerService(IGlobalSelectionProvider.class, this, null);
	}

	@Override
	public void unregister() {
		if (registerService == null)
			return;
		for (Map.Entry<IGlobalSelectionListener, ServiceRegistration<IGlobalSelectionListener>> entry : registrationMap.entrySet()) {
			entry.getValue().unregister();
		}
		registerService.unregister();
	}

	@Override
	public <T> void setSelection(Class<T> clazz, T selection) {
		if (service != null)
			service.setSelection(clazz, selection);
	}

	@Override
	public <T> void updateSelection(Class<T> clazz) {
		if (service != null)
			service.updateSelection(clazz);
	}
	
	@Override
	public <T> T getSelection(Class<T> clazz) {
		if (service == null)
			return null;
		return service.getSelection(clazz);
	}

	@Override
	public void registerListener(IGlobalSelectionListener<?> listener) {
		ServiceRegistration<IGlobalSelectionListener> registration;
		registration = bundleContext.registerService(IGlobalSelectionListener.class, listener, null);
		registrationMap.put(listener, registration);
	}

	@Override
	public void unregisterListener(IGlobalSelectionListener<?> listener) {
		ServiceRegistration<IGlobalSelectionListener> registration = registrationMap.get(listener);
		if (registration == null)
			return;
		registration.unregister();
		registrationMap.remove(registration);
	}

	@Override
	public void setGlobalSelectionService(IGlobalSelectionService service) {
		if (service != null)
			this.service = service;
		else
			throw new IllegalArgumentException("GlobalSelectionProvider.setGlobalSelectionService() -> PARAM MUST NOT NULL");
	}
}
