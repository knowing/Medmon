package de.lmu.ifi.dbs.medmon.medic.core.util;

import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;

public abstract class JFaceUtil {

	/**
	 * sets first item as default selection if possible and returns this
	 * selection
	 */
	@SuppressWarnings("unchecked")
	public static <T> T initializeViewerSelection(Class<T> returnType, Viewer viewer) {
		if (!viewer.getSelection().isEmpty())
			return (T) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		Object input = viewer.getInput();
		if (input instanceof Object[]) {
			Object[] array = (Object[]) input;
			if (array.length != 0) {
				viewer.setSelection(new StructuredSelection(array[0]));
				return (T) array[0];
			}
		} else if (input instanceof Collection) {
			Collection<?> collection = (Collection<?>) input;
			Object[] array = collection.toArray();
			if (collection.size() != 0) {
				viewer.setSelection(new StructuredSelection(array[0]));
				return (T) array[0];
			}
		}
		return null;
	}
}
