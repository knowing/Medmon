package de.lmu.ifi.dbs.medmon.medic.core.service;



/**
 * <p>Used by UI components to propagate selection changes
 * to the @IGlobalSelectionService </p>
 * 
 * @author Nepomuk Seiler
 * @since 2011-10-14
 * @version 1.0
 * 
 */
public interface IGlobalSelectionProvider<T> {

	/**
	 * Called by the UI component
	 * @param clazz - the selection key
	 * @param selection - the current selection
	 */
	public void setSelection(T selection);
	
	/**
	 * Called by the @IGlobalSelectionService to register itself.
	 * @param service - the @IGlobalSelectionService
	 */
	public void setGlobalSelectionService(IGlobalSelectionService service);
}
