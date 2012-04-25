package de.lmu.ifi.dbs.medmon.base.ui.adapter;

import java.text.DateFormat;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;

import base.ui.icons.Icons;

import de.lmu.ifi.dbs.medmon.base.ui.Activator;
import de.lmu.ifi.dbs.medmon.base.ui.util.SWTResourceManager;
import de.lmu.ifi.dbs.medmon.database.entity.Data;
import de.lmu.ifi.dbs.medmon.services.IPatientService;

public class DataColumnAdapter implements IWorkbenchColumnAdapter {

	private final DateFormat	df	= DateFormat.getDateTimeInstance();

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
		return null;
	}

	@Override
	public Object getParent(Object o) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		Data data = (Data) element;
		switch (columnIndex) {
		case 1:
			return df.format(data.getFrom());
		case 2:
			return df.format(data.getTo());
		case 3:
			return data.getSensor().getName();
		case 4:
			if (data.getTherapyResult() == null)
				return null;
			return data.getTherapyResult().getCaption();
		default:
			return null;
		}
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		Data data = (Data) element;
		switch (columnIndex) {
		case 0:
			return Activator.getImageDescriptor("/icons/" + data.getType() + ".gif").createImage();
		}
		return null;
	}
}
