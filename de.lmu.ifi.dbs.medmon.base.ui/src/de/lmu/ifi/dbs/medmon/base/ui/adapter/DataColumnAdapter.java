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
		Data adapter = (Data) element;
		switch (columnIndex) {
		case 0:
			return adapter.getPatient().toString();
		case 1:
			return df.format(adapter.getFrom());
		case 2:
			return df.format(adapter.getTo());
		default:
			return "-";
		}
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

}
