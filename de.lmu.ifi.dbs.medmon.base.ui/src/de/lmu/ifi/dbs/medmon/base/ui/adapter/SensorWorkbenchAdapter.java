package de.lmu.ifi.dbs.medmon.base.ui.adapter;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.base.ui.Activator;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

public class SensorWorkbenchAdapter implements IWorkbenchColumnAdapter {

	private static final Logger log = LoggerFactory.getLogger(Activator.PLUGIN_ID);
	
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
		return ((ISensor)o).getName();
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
			return sensor.getName();
		case 1:
			return sensor.getVersion();
		default:
			return "-";
		}
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
}
