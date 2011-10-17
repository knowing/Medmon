package de.lmu.ifi.dbs.medmon.medic.core.service;

/**
 * 
 * @author Nepomuk Seiler
 * @since 2011-10-14
 * @version 0.1
 */
public interface IGlobalSelectionListener {

	/**
	 * Callback for UI components
	 * @param clazz
	 * @param Selection
	 */
	<T> void selectionChanged(Class<T> clazz, T Selection);
}
