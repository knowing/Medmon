package de.lmu.ifi.dbs.medmon.medic.core.service;

/**
 * 
 * @author Nepomuk Seiler, Stephan Picker
 * @since 2011-10-14
 * @version 0.2
 */
public interface IGlobalSelectionListener<T> {

	/**
	 * notifies the listener on selection changes
	 * @param selection - selected object
	 */
	void selectionChanged(T selection);
	
	/**
	 * notifies the listener on updated
	 */
	void selectionUpdated();
	
	/**
	 * the returned class type specifies the class this listener listens to
	 * @param clazz - class of selected object
	 */
	Class<T> getType();
}
