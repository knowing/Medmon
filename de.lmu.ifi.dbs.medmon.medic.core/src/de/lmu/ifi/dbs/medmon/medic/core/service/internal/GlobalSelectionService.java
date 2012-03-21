package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.Activator;
import de.lmu.ifi.dbs.medmon.medic.core.service.IEntityManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionListener;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionService;

public class GlobalSelectionService implements IGlobalSelectionService {
	// ********************************************************************************
	private final Logger									log					= LoggerFactory.getLogger(IGlobalSelectionService.class);
	private List<IGlobalSelectionProvider>					providerServices	= new ArrayList<IGlobalSelectionProvider>();
	private Map<Class<?>, Set<IGlobalSelectionListener<?>>>	listenerServices	= new HashMap<Class<?>, Set<IGlobalSelectionListener<?>>>();
	private Map<Class<?>, Object>							selectionMap		= new HashMap<Class<?>, Object>();
	private EntityManager									workerEM;

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getSelection(Class<T> clazz) {
		return (T) selectionMap.get(clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T setSelection(Class<T> clazz, T selection) {
		T oldSelection = (T) selectionMap.put(clazz, selection);
		if (listenerServices.get(clazz) == null) {
			return oldSelection;
		}
		for (IGlobalSelectionListener<?> listener : listenerServices.get(clazz))
			((IGlobalSelectionListener<T>) listener).selectionChanged(selection);
		return oldSelection;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> void updateSelection(Class<T> clazz) {
		T selection = (T) selectionMap.get(clazz);
		if (selection == null || listenerServices.get(clazz) == null)
			return;
		for (IGlobalSelectionListener<?> listener : listenerServices.get(clazz))
			((IGlobalSelectionListener<T>) listener).selectionUpdated();
	}

	private void savePatient() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		if (getSelection(Patient.class) != null) {
			preferenceStore.setValue(Patient.class.toString(), getSelection(Patient.class).getId());
		}
	}

	private void loadPatient() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		long patientId = preferenceStore.getLong(Patient.class.toString());
		Patient patient = workerEM.find(Patient.class, patientId);
		workerEM.clear();

		if (patient != null) {
			setSelection(Patient.class, patient);
		}
	}

	protected void activate(ComponentContext context) {
		log.info("GlobalSelectionService started successfully");
	}

	protected void deactivate(ComponentContext context) {
		savePatient();
	}

	protected void bindProvider(IGlobalSelectionProvider provider) {
		providerServices.add(provider);
		provider.setGlobalSelectionService(this);
	}

	protected void unbindProvider(IGlobalSelectionProvider provider) {
		providerServices.remove(provider);
	}

	public void bindEntityManagerService(IEntityManagerService service) {
		if (workerEM == null) {
			workerEM = service.createEntityManager();
			loadPatient();
		}
	}

	public void unbindEntityManagerService(IEntityManagerService service) {
		workerEM.close();
		workerEM = null;
	}

	@SuppressWarnings("unchecked")
	protected <T> void bindListener(IGlobalSelectionListener<T> listener) {
		if (listenerServices.get(listener.getType()) == null) {
			listenerServices.put(listener.getType(), new HashSet<IGlobalSelectionListener<?>>());
		}
		listenerServices.get(listener.getType()).add(listener);
		T currentSelection = (T) selectionMap.get(listener.getType());
		if (currentSelection != null)
			listener.selectionChanged(currentSelection);
	}

	protected void unbindListener(IGlobalSelectionListener<?> listener) {
		listenerServices.get(listener.getType()).remove(listener);
	}
}
