package de.lmu.ifi.dbs.medmon.base.ui.adapter;

import java.text.DateFormat;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import de.lmu.ifi.dbs.medmon.database.model.Data;

public class DataColumnAdapter implements IWorkbenchColumnAdapter {

	private final DateFormat df = DateFormat.getDateTimeInstance();
	
	
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
		return "";
	}

	@Override
	public Object getParent(Object o) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		Data data = (Data) element;
		switch (columnIndex) {
		case 0:
			return df.format(data.getFrom());
		case 1:
			return df.format(data.getTo());
		case 2:
			return data.getSensor().getName();
		case 3:
			if(data.getTherapyResult() == null)
				return "";
			return data.getTherapyResult().getCaption();
		default:
			return "-";
		}
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
}
