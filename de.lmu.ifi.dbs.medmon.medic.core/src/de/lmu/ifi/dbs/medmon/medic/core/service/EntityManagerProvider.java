package de.lmu.ifi.dbs.medmon.medic.core.service;

import java.util.Map;

import javax.persistence.EntityManager;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class EntityManagerProvider implements IEntityManagerProvider {

	private ServiceRegistration<IEntityManagerProvider> registration;
	private IEntityManagerService service = null;

	public EntityManagerProvider(BundleContext bundleContext) {
		if (bundleContext == null)
			throw new NullPointerException("EntityManagerProvider() => bundleContext mustnot be null");
		registration = bundleContext.registerService(IEntityManagerProvider.class, this, null);
	}

	@Override
	public void unregister() {
		if (registration == null)
			return;
		registration.unregister();
	}

	@Override
	public EntityManager getEntityManager() {
		if (service == null)
			return null;
		return service.getEntityManager();
	}

	@Override
	public void setEntityManagerService(IEntityManagerService serivce) {
		this.service = serivce;
	}

}
