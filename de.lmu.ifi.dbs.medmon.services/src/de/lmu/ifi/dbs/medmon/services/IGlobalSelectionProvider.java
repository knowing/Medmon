package de.lmu.ifi.dbs.medmon.services;

/**
 * <p>Used by UI components to propagate selection changes
 * to the @IGlobalSelectionService </p>
 * 
 * @author Nepomuk Seiler
 * @since 2011-10-14
 * @version 2.0
 * 
 */
public interface IGlobalSelectionProvider {

	/**
	 * unregisters this service and all listener which have been registered using this provider
	 */
	public void unregister();
	
	/**
	 * Called by the UI component
	 * @param clazz - class of selected object
	 * @param selection - selected object
	 */
	public <T> void setSelection(Class<T> clazz, T selection);
	
	/**
	 * Called by the UI component
	 * @param clazz - class of selected object
	 * @param selection - selected object
	 */
	public <T> void updateSelection(Class<T> clazz);
	
	/**
	 * Called by the UI component
	 * @param clazz - the selection key
	 * @return selected object
	 */
	public <T> T getSelection(Class<T> clazz);
	
	/**
	 * @param listener - a listener to register
	 */
	public void registerSelectionListener(IGlobalSelectionListener<?> listener);
	
	/**
	 * @param listener - a listener to unregister
	 */
	public void unregisterSelectionListener(IGlobalSelectionListener<?> listener);
	
	/**
	 * Called by the @IGlobalSelectionService to register itself.
	 * @param service - the @IGlobalSelectionService
	 */
	public void setGlobalSelectionService(IGlobalSelectionService service);
}
