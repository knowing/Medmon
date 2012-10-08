package de.lmu.ifi.dbs.medmon.base.ui.adapter;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;

import de.lmu.ifi.dbs.medmon.base.ui.Activator;
import de.lmu.ifi.dbs.medmon.base.ui.util.ResourceManager;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

public class SensorWorkbenchAdapter implements IAdapterFactory {

    private static final Class[] types = new Class[] { IWorkbenchColumnAdapter.class, IWorkbenchAdapter.class };

    @Override
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (!(adaptableObject instanceof ISensor))
            return null;
        if (adapterType.equals(IWorkbenchColumnAdapter.class))
            return new SensorWorkbenchAdapterImpl();
        else if (adapterType.equals(IWorkbenchAdapter.class))
            return new SensorWorkbenchAdapterImpl();

        return null;
    }

    @Override
    public Class[] getAdapterList() {
        return types;
    }

    private class SensorWorkbenchAdapterImpl implements IWorkbenchColumnAdapter {
        @Override
        public Object[] getChildren(Object o) {
            return null;
        }

        @Override
        public ImageDescriptor getImageDescriptor(Object object) {
            return null;
        }

        @Override
        public String getLabel(Object o) {
            return ((ISensor) o).getName();
        }

        @Override
        public Object getParent(Object o) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            ISensor sensor = (ISensor) element;
            switch (columnIndex) {
            case 0:
                return "";
            case 1:
                return sensor.getName();
            case 2:
                return sensor.getSerial();
            default:
                return "-";
            }
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            ISensor sensor = (ISensor) element;
            switch (columnIndex) {
            case 0:
                ImageDescriptor descriptor = null;
                if (sensor.isInstance()) {
                    descriptor = Activator.getImageDescriptor("/icons/sensor_24.png");
                } else if (sensor.getDriver() == null) {
                    descriptor = Activator.getImageDescriptor("/icons/dialog_error_24.png");
                } else {
                    descriptor = Activator.getImageDescriptor("/icons/sensor_disabled_24.png");
                }

                return ResourceManager.getImage(descriptor);
            default:
                return null;
            }
        }
    }
}
