package de.lmu.ifi.dbs.medmon.base.ui.viewer;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.base.ui.Activator;
import de.lmu.ifi.dbs.medmon.base.ui.provider.WorkbenchTableLabelProvider;
import de.lmu.ifi.dbs.medmon.database.model.Sensor;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.ISensorObserver;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

public class SensorTableViewer extends TableViewer {

	private static final Logger						log				= LoggerFactory.getLogger(Activator.PLUGIN_ID);
	private ServiceRegistration<ISensorObserver>	serviceRegistration;
	Menu											popUpMenu;
	Shell											shell;
	List<ISensor>									localSensorList	= new LinkedList<ISensor>();

	public SensorTableViewer(Composite parent, int style) {
		super(parent, style);
		shell = getTable().getShell();
		init();
	}

	private void init() {
		initMenu();
		initColumns();
		initProvider();
		initInput();
	}

	public void initMenu() {
		popUpMenu = new Menu(shell, SWT.POP_UP);
		MenuItem itemSetDefaultPath = new MenuItem(popUpMenu, SWT.PUSH);
		itemSetDefaultPath.setText("setze Standard-Pfad");
		itemSetDefaultPath.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent event) {
				IGlobalSelectionProvider selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
				ISensor selection = selectionProvider.getSelection(ISensor.class);
				selectionProvider.unregister();

				if (selection == null)
					return;

				DirectoryDialog dlg = new DirectoryDialog(shell);
				dlg.setText("Standard-Pfad");
				dlg.setMessage("wählen sie den neuen Standard-Pfad für diesen Sensor aus!");
				String dir = dlg.open();
				if (dir != null) {
					Sensor mSensor = Activator.getSensorManagerService().loadSensorEntity(selection);
					if (mSensor == null)
						return;

					EntityManager entityManager = JPAUtil.createEntityManager();

					entityManager.getTransaction().begin();
					mSensor = entityManager.merge(mSensor);
					mSensor.setDefaultpath(dir);
					entityManager.getTransaction().commit();

					entityManager.close();

					Activator.getSensorManagerService().notifySensorObservers(selection);
				}
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});

		MenuItem itemImport = new MenuItem(popUpMenu, SWT.PUSH);
		itemImport.setText("importieren");

		getTable().setMenu(popUpMenu);
	}

	private void initColumns() {
		getTable().setHeaderVisible(true);
		getTable().setLinesVisible(true);

		TableViewerColumn viewerColumnName = new TableViewerColumn(this, SWT.LEAD);
		viewerColumnName.getColumn().setText("Name");
		viewerColumnName.getColumn().setWidth(150);
		viewerColumnName.getColumn().setResizable(true);
		viewerColumnName.getColumn().setMoveable(true);
		TableViewerColumn viewerColumnVersion = new TableViewerColumn(this, SWT.LEAD);
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
		addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.isEmpty())
					return;

				IGlobalSelectionProvider selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
				selectionProvider.setSelection(ISensor.class, (ISensor) selection.getFirstElement());
				selectionProvider.unregister();
			}
		});

		ISensorObserver sensorObserver = new ISensorObserver() {
			@Override
			public void init(List<ISensor> sensors) {
				localSensorList = new LinkedList<ISensor>(sensors);
				setInput(localSensorList);
			}

			@Override
			public void sensorAdded(ISensor service) {
				localSensorList.add(service);
				setInput(localSensorList);
			}

			@Override
			public void sensorRemoved(ISensor service) {
				localSensorList.remove(service);
				setInput(localSensorList);
			}

			@Override
			public void sensorUpdated(ISensor service) {
				refresh();
			}
		};
		serviceRegistration = Activator.getBundleContext().registerService(ISensorObserver.class, sensorObserver, null);
	}

	public void dispose() {
		serviceRegistration.unregister();
	}

}
