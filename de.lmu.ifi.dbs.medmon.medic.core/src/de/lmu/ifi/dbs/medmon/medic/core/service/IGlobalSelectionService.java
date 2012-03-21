package de.lmu.ifi.dbs.medmon.medic.core.service;

import de.lmu.ifi.dbs.medmon.database.entity.Patient;

/**
 * 
 * @author Nepomuk Seiler
 * @since 2011-10-14
 * @version 1.0
 */
public interface IGlobalSelectionService {

	/**
	 * 
	 * @param clazz
	 * @return current selection or null
	 */
	<T> T getSelection(Class<T> clazz);

	/**
	 * 
	 * @param clazz
	 * @param selection
	 * @return old selection
	 */
	<T> T setSelection(Class<T> clazz, T selection);
	
	/**
	 * @param clazz
	 */
	<T> void updateSelection(Class<T> clazz);

}