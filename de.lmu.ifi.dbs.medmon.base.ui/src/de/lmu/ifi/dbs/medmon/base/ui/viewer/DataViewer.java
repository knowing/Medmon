package de.lmu.ifi.dbs.medmon.base.ui.viewer;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import de.lmu.ifi.dbs.medmon.base.ui.provider.WorkbenchTableLabelProvider;
import de.lmu.ifi.dbs.medmon.database.entity.Data;

public class DataViewer extends TableViewer {

	private static final String[]	columns		= new String[] { "Type", "Von", "Bis", "Sensor", "Ergebnisse" };
	private static final int[]		width		= new int[] { 60, 150, 150, 200, 200 };
	private ViewerComparator[]		comparator	= new ViewerComparator[4];

	public DataViewer(Composite parent, int style) {
		super(parent, style);
		init();
	}

	public DataViewer(Table table) {
		super(table);
		init();
	}

	private void init() {
		initComparators();
		initColumns();
		initProvider();
	}

	private void initComparators() {
		for (int i = 1; i < 4; i++) {
			final int column = i;
			comparator[i] = new ViewerComparator() {
				@Override
				public int compare(Viewer viewer, Object e1, Object e2) {
					WorkbenchTableLabelProvider lp = (WorkbenchTableLabelProvider) getLabelProvider();
					return lp.getColumnText(e1, column).compareTo(lp.getColumnText(e2, column));
				}
			};
		}
	}

	private void initColumns() {
		getTable().setHeaderVisible(true);
		getTable().setLinesVisible(true);
		for (int i = 0; i < columns.length; i++) {
			final int column = i;
			TableViewerColumn viewerColumn = new TableViewerColumn(this, SWT.LEAD);
			// Kopfzeile und Breite der jeweiligen Spalten festlegen
			viewerColumn.getColumn().setText(columns[i]);
			viewerColumn.getColumn().setWidth(width[i]);
			// Spaltengroesse laesst sich zur Laufzeit aendern
			viewerColumn.getColumn().setResizable(true);
			// Spalten lassen sich untereinander verschieben
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setComparator(comparator[column]);
				}
			});
		}
	}

	private void initProvider() {
		setContentProvider(new ArrayContentProvider());
		setLabelProvider(new WorkbenchTableLabelProvider());
	}

}
