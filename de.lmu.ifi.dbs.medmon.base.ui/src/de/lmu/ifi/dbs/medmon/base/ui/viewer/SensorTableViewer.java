package de.lmu.ifi.dbs.medmon.base.ui.viewer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
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
	
	private static final String[] columns = new String[] {"", "Name", "Version"};
	private static final int[] width = new int[] { 24, 200, 150 };

    private Menu popUpMenu;
    private ViewerFilter driverFilter;

    public SensorTableViewer(Composite parent, int style) {
        super(parent, style);
        init();
    }

    private void init() {
        initMenu();
        initColumns();
        initProvider();
        initInput();
        initFilter();
    }

    public void initMenu() {
        popUpMenu = new Menu(getControl().getShell(), SWT.POP_UP);

        final MenuItem itemImport = new MenuItem(popUpMenu, SWT.PUSH);
        itemImport.setText("Sensordatei importieren");

        final MenuItem itemDriver = new MenuItem(popUpMenu, SWT.CHECK);
        itemDriver.setText("Treiber anzeigen");
        itemDriver.setEnabled(true);
        itemDriver.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (itemDriver.getSelection()) {
                    removeFilter(driverFilter);
                } else {
                    addFilter(driverFilter);
                }
            }
        });

        getControl().setMenu(popUpMenu);
    }

    private void initColumns() {
        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);
        
        for (int i = 0; i < columns.length; i++) {
            TableViewerColumn viewerColumnName = new TableViewerColumn(this, SWT.LEAD);
            viewerColumnName.getColumn().setText(columns[i]);
            viewerColumnName.getColumn().setWidth(width[i]);
            viewerColumnName.getColumn().setResizable(true);
            viewerColumnName.getColumn().setMoveable(true);
		}

    }

    private void initProvider() {
        setContentProvider(new ArrayContentProvider());
        setLabelProvider(new WorkbenchTableLabelProvider());
    }

    private void initInput() {
        setInput(getSensors());
        Activator.getSensorManagerService().addListener(new ISensorListener() {

            @Override
            public void sensorChanged(SensorEvent event) {
                if (getControl().isDisposed())
                    return;
                getControl().getDisplay().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        setInput(getSensors());
                    }
                });
            }
        });

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
    }

    private void initFilter() {
        driverFilter = new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                ISensor sensor = (ISensor) element;
                return sensor.isInstance();
            }
        };
        addFilter(driverFilter);
    }

    private List<ISensor> getSensors() {
        List<ISensor> instances = Activator.getSensorManagerService().getConnectedSensors();
        List<ISensor> services = Activator.getSensorManagerService().getSensors();
        List<ISensor> sensors = new ArrayList<>(instances.size() + services.size());
        sensors.addAll(instances);
        sensors.addAll(services);
        return sensors;
    }

}
