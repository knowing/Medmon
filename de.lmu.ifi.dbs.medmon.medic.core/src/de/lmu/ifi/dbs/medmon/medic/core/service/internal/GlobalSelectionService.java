package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionListener;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionService;

public class GlobalSelectionService implements IGlobalSelectionService {
	// ********************************************************************************
	private final Logger log = LoggerFactory.getLogger(IGlobalSelectionService.class);
	private List<IGlobalSelectionProvider> providerServices = new ArrayList<IGlobalSelectionProvider>();
	private Map<Class<?>, Set<IGlobalSelectionListener<?>>> listenerServices = new HashMap<Class<?>, Set<IGlobalSelectionListener<?>>>();
	private Map<Class<?>, Object> selectionMap = new HashMap<Class<?>, Object>();

	// ********************************************************************************
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getSelection(Class<T> clazz) {
		return (T) selectionMap.get(clazz);
	}

	// ********************************************************************************
	@SuppressWarnings("unchecked")
	@Override
	public <T> T setSelection(Class<T> clazz, T selection) {
		T oldSelection = (T) selectionMap.put(clazz, selection);
		if (listenerServices.get(clazz) == null || selection.equals(oldSelection)) {
			return oldSelection;
		}
		for (IGlobalSelectionListener<?> listener : listenerServices.get(clazz))
			((IGlobalSelectionListener<T>) listener).selectionChanged(selection);
		return oldSelection;
	}

	// ********************************************************************************
	protected void activate(ComponentContext context) {
		log.info("GlobalSelectionService started successfully");
	}

	// ********************************************************************************
	protected void bindProvider(IGlobalSelectionProvider provider) {
		providerServices.add(provider);
		provider.setGlobalSelectionService(this);
	}

	// ********************************************************************************
	protected void unbindProvider(IGlobalSelectionProvider provider) {
		providerServices.remove(provider);
		// provider.setGlobalSelectionService(null); ???
	}

	// ********************************************************************************
	protected void bindListener(IGlobalSelectionListener<?> listener) {
		if (listenerServices.get(listener.getType()) == null) {
			listenerServices.put(listener.getType(), new HashSet<IGlobalSelectionListener<?>>());
		}
		listenerServices.get(listener.getType()).add(listener);
		log.debug("bound:" + listener.getType());
	}

	// ********************************************************************************
	protected void unbindListener(IGlobalSelectionListener<?> listener) {
		listenerServices.get(listener.getType()).remove(listener);
	}
	// ********************************************************************************
}
