package de.lmu.ifi.dbs.medmon.medic.core.selection;

/**
 * 
 * @author Nepomuk Seiler
 * @since 2011-10-14
 * @version 0.1
 */
public interface IGlobalSelectionListener<T> {

	/**
	 * Callback for UI components
	 * @param clazz
	 * @param selection
	 */
	void selectionChanged(T selection);
	
	/**
	 * 
	 * @return equals T
	 */
	Class<T> getType();
}
