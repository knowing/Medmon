package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionListener;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionService;

public class GlobalSelectionService implements IGlobalSelectionService {

	private final Logger log = LoggerFactory.getLogger(IGlobalSelectionService.class);
	
	private List<IGlobalSelectionListener<?>>	listenerServices	= new ArrayList<IGlobalSelectionListener<?>>();
	private List<IGlobalSelectionProvider<?>>	providerServices	= new ArrayList<IGlobalSelectionProvider<?>>();
	private Map<Class<?>, Object>				selectionMap		= new HashMap<Class<?>, Object>();

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getSelection(Class<T> clazz) {
		return (T) selectionMap.get(clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	//looks ugly - which in fact ... it is !
	public <T> T setSelection(Class<T> clazz, T selection) {
		T oldSelection = (T) selectionMap.get(clazz);
		selectionMap.put(clazz, selection);
		for (IGlobalSelectionListener<?> listener : listenerServices) {
			if (listener.getType() == clazz) {
				((IGlobalSelectionListener<T>) listener).selectionChanged(selection);
			}
		}
		return oldSelection;
	}

	protected void activate(ComponentContext context) {
		log.info("GlobalSelectionService started successfully");
	}

	@SuppressWarnings("unused")
	private void bindProvider(IGlobalSelectionProvider<?> provider) {
		providerServices.add(provider);
	}

	@SuppressWarnings("unused")
	private void unbindProvider(IGlobalSelectionProvider<?> provider) {
		providerServices.remove(provider);
	}

	@SuppressWarnings("unused")
	private void bindListener(IGlobalSelectionListener<?> listener) {
		listenerServices.add(listener);
	}

	@SuppressWarnings("unused")
	private void unbindListener(IGlobalSelectionListener<?> listener) {
		listenerServices.remove(listener);
	}
}
