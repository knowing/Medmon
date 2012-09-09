package de.lmu.ifi.dbs.medmon.base.ui.viewer;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import de.lmu.ifi.dbs.medmon.base.ui.Activator;
import de.lmu.ifi.dbs.medmon.base.ui.provider.WorkbenchTableLabelProvider;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensorListener;
import de.lmu.ifi.dbs.medmon.sensor.core.SensorEvent;
import de.lmu.ifi.dbs.medmon.services.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.services.IGlobalSelectionProvider;

public class SensorTableViewer extends TableViewer {

	private Menu popUpMenu;

	public SensorTableViewer(Composite parent, int style) {
		super(parent, style);
		init();
	}

	private void init() {
		initMenu();
		initColumns();
		initProvider();
		initInput();
	}

	public void initMenu() {
		popUpMenu = new Menu(getTable().getShell(), SWT.POP_UP);
		MenuItem itemSetDefaultPath = new MenuItem(popUpMenu, SWT.PUSH);
		itemSetDefaultPath.setText("setze Standard-Pfad");
		itemSetDefaultPath.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				IGlobalSelectionProvider selectionProvider = GlobalSelectionProvider
						.newInstance(Activator.getBundleContext());
				ISensor selection = selectionProvider
						.getSelection(ISensor.class);
				selectionProvider.unregister();

				if (selection == null)
					return;

				DirectoryDialog dlg = new DirectoryDialog(getTable().getShell());
				dlg.setText("Standard-Pfad");
				dlg.setMessage("w\u00e4hlen sie den neuen Standard-Pfad f\u00fcr diesen Sensor aus!");
				String dir = dlg.open();
				// FIXME implement SensorTableViewer

				if (dir != null) {
					/*Sensor mSensor = Activator.getSensorManagerService()
							.loadSensorEntity(selection);
					if (mSensor == null)
						return;

					EntityManager entityManager = Activator
							.getEntityManagerService().createEntityManager();

					entityManager.getTransaction().begin();
					mSensor = entityManager.merge(mSensor);
					mSensor.setDefaultpath(dir);
					entityManager.getTransaction().commit();

					entityManager.close();*/
				}

			}

		});

		MenuItem itemImport = new MenuItem(popUpMenu, SWT.PUSH);
		itemImport.setText("importieren");

		getTable().setMenu(popUpMenu);
	}

	private void initColumns() {
		getTable().setHeaderVisible(true);
		getTable().setLinesVisible(true);

		TableViewerColumn viewerColumnName = new TableViewerColumn(this,
				SWT.LEAD);
		viewerColumnName.getColumn().setText("Name");
		viewerColumnName.getColumn().setWidth(150);
		viewerColumnName.getColumn().setResizable(true);
		viewerColumnName.getColumn().setMoveable(true);
		TableViewerColumn viewerColumnVersion = new TableViewerColumn(this,
				SWT.LEAD);
		viewerColumnVersion.getColumn().setText("Version");
		viewerColumnVersion.getColumn().setWidth(150);
		viewerColumnVersion.getColumn().setResizable(true);
		viewerColumnVersion.getColumn().setMoveable(true);
	}

	private void initProvider() {
		setContentProvider(new ArrayContentProvider());
		setLabelProvider(new WorkbenchTableLabelProvider());
	}

	private void initInput() {
		List<ISensor> sensors = Activator.getSensorManagerService()
				.getConnectedSensors();
		setInput(sensors);
		Activator.getSensorManagerService().addListener(new ISensorListener() {

			@Override
			public void sensorChanged(SensorEvent event) {
				getControl().getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {
						setInput(Activator.getSensorManagerService()
								.getConnectedSensors());
					}
				});
			}
		});

		addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				if (selection.isEmpty())
					return;

				IGlobalSelectionProvider selectionProvider = GlobalSelectionProvider
						.newInstance(Activator.getBundleContext());
				selectionProvider.setSelection(ISensor.class,
						(ISensor) selection.getFirstElement());
				selectionProvider.unregister();
			}
		});
	}

}
