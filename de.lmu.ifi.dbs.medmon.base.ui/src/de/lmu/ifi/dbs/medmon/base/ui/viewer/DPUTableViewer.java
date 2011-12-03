package de.lmu.ifi.dbs.medmon.base.ui.viewer;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.knowing.core.model.IDataProcessingUnit;
import de.lmu.ifi.dbs.medmon.base.ui.Activator;
import de.lmu.ifi.dbs.medmon.base.ui.provider.WorkbenchTableLabelProvider;

/**
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 08.05.2011
 * 
 */
public class DPUTableViewer extends TableViewer {

	private final Logger log = LoggerFactory.getLogger(Activator.PLUGIN_ID);
	
	private static final String[] columns = new String[] { "Name", "Beschreibung", "Tags"};
	private static final int[] width = new int[] { 180, 220, 100 };

	/**
	 * @param parent
	 * @param style
	 */
	public DPUTableViewer(Composite parent, int style) {
		super(parent, style);
		init();
	}

	/**
	 * 
	 * @param table
	 */
	public DPUTableViewer(Table table) {
		super(table);
		init();
	}

	private void init() {
		initColumns();
		initProvider();
		initInput();
	}

	private void initColumns() {
		getTable().setHeaderVisible(true);
		getTable().setLinesVisible(true);
		for (int i = 0; i < columns.length; i++) {
			TableViewerColumn viewerColumn = new TableViewerColumn(this, SWT.LEAD);
			// Kopfzeile und Breite der jeweiligen Spalten festlegen
			viewerColumn.getColumn().setText(columns[i]);
			viewerColumn.getColumn().setWidth(width[i]);
			// Spaltengroesse laesst sich zur Laufzeit aendern
			viewerColumn.getColumn().setResizable(true);
			// Spalten lassen sich untereinander verschieben
			viewerColumn.getColumn().setMoveable(true);
		}
	}

	private void initProvider() {
		setContentProvider(new ArrayContentProvider());
		setLabelProvider(new WorkbenchTableLabelProvider());
	}

	private void initInput() {
		IDataProcessingUnit[] dpus = Activator.getDPUDirectory().getDPUs();
		setInput(dpus);
	}
}
