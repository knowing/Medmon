package de.lmu.ifi.dbs.medmon.medic.core.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

public class GlobalSelectionService implements IGlobalSelectionService {

	private BundleContext						bundleContext		= null;
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

	@SuppressWarnings("unused")
	private void activate(ComponentContext context) {
		bundleContext = context.getBundleContext();
		System.out.print("************************<STARTED>***************************");
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
