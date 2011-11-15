package de.lmu.ifi.dbs.medmon.base.ui.adapter;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.base.ui.Activator;

public class SensorColumnAdapter implements IWorkbenchColumnAdapter {

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
		log.debug("SensorColumnAdapter::getLabel()");
		return null;
	}

	@Override
	public Object getParent(Object o) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		log.debug("SensorColumnAdapter::getColumnText()");
		return null;
		/*
		SensorAdapter adapter = (SensorAdapter) element;
		switch (columnIndex) {
		case 0:
			return adapter.getName();
		case 1:
			return adapter.getVersion();
		case 2:
			return adapter.getType();
		case 3:
			return adapter.getDefaultPath();
		case 4:
			return status(adapter.isAvailable());
		default:
			return "-";
		}
		*/
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		log.debug("SensorColumnAdapter::getColumnImage()");
		/*
		if (columnIndex == 4) {
			SensorAdapter adapter = (SensorAdapter) element;
			if (adapter.isAvailable())
				return Activator.getImageDescriptor("icons/sensor_16.png").createImage();
			return Activator.getImageDescriptor("icons/sensor_disabled_16.png").createImage();
		}*/

		return null;
	}
	
	private String status(boolean available) {
		if(available)
			return "ready";
		return "";
	}

}
