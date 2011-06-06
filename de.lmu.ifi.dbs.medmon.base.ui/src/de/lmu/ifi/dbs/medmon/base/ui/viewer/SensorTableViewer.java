package de.lmu.ifi.dbs.medmon.base.ui.viewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

import de.lmu.ifi.dbs.medmon.base.ui.provider.WorkbenchTableLabelProvider;
import de.lmu.ifi.dbs.medmon.base.ui.viewer.editing.SensorPathEditingSupport;
import de.lmu.ifi.dbs.medmon.medic.core.sensor.SensorAdapter;
import de.lmu.ifi.dbs.medmon.medic.core.sensor.SensorDaemon;

public class SensorTableViewer extends TableViewer implements PropertyChangeListener {

	private static final String[] columns = new String[] { "Name", "Version", "Typ", "Pfad", "Status" };
	private static final int[] width = new int[] { 120, 70, 60, 150, 80 };

	public SensorTableViewer(Composite parent, int style) {
		super(parent, style);
		init();
	}

	public SensorTableViewer(Composite parent, int style, IStructuredSelection initialSelection) {
		super(parent, style);
		init();
		if (initialSelection != null && !initialSelection.isEmpty())
			setSelection(initialSelection);
	}

	public SensorTableViewer(Table table) {
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
			if (i == 3) {
				viewerColumn.setEditingSupport(new SensorPathEditingSupport(this));
			}
		}
	}

	private void initProvider() {
		setContentProvider(new ArrayContentProvider());
		setLabelProvider(new WorkbenchTableLabelProvider());
	}

	private void initInput() {
		SensorDaemon daemon = SensorDaemon.getDaemon();
		if (daemon == null) {
			new Thread(new WaitingForInput(this)).start();
		} else {
			Map<String, SensorAdapter> model = daemon.getModel();
			if(model == null) {
				new Thread(new WaitingForInput(this)).start();
			} else {
				setInput(model.values().toArray());
				daemon.addPropertyChangeListener(this);
			}

		}

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				if (!getTable().isDisposed())
					refresh();
			}
		});
	}

	private class WaitingForInput implements Runnable {

		private final SensorTableViewer viewer;

		public WaitingForInput(SensorTableViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public void run() {
			SensorDaemon daemon = SensorDaemon.getDaemon();
			int timer = 1;
			//Try getting the Daemon
			while (daemon == null && timer < 7 ) {
				daemon = SensorDaemon.getDaemon();
				try {
					Thread.sleep(timer++ * 500L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			final Map<String, SensorAdapter> model = daemon.getModel();
			
			//Access not from the UI Thread
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					int timer = 1;
					Collection<SensorAdapter> values = model.values();
					while(values == null && timer < 7) {
						try {
							Thread.sleep(timer++ * 500L);
							values = model.values();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					//TODO SensorTableViewer check NullPointer
					if(timer != 7) {
						viewer.setInput(values.toArray());
						viewer.refresh();
					}
				
				}
			});

			daemon.addPropertyChangeListener(viewer);
		}

	}

}
