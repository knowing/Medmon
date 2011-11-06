package de.lmu.ifi.dbs.medmon.medic.core.service;

/**
 * 
 * @author Nepomuk Seiler
 * @since 2011-10-14
 * @version 0.1
 */
public interface IGlobalSelectionListener<T> {


	void selectionChanged(T selection);
	

	Class<T> getType();
}
