package de.lmu.ifi.dbs.medmon.base.ui.adapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

import de.lmu.ifi.dbs.medmon.database.entity.Therapy;

/**
 *
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 2011
 */
public class TherapyColumnAdapter implements IAdapterFactory {

	private static final Class[]	types	= new Class[] { IWorkbenchAdapter.class };
	private static final DateFormat	df		= new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType.equals(IWorkbenchAdapter.class) && adaptableObject instanceof Therapy)
			return new TherapyColumnAdapterImpl();
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return types;
	}

	public class TherapyColumnAdapterImpl implements IWorkbenchAdapter {

		public Object[] getChildren(Object o) {
			return ((Therapy) o).getTherapyResults().toArray();
		}

		public ImageDescriptor getImageDescriptor(Object object) {
			return null;
		}

		public String getLabel(Object o) {
			Therapy t = (Therapy) o;
			StringBuilder sb = new StringBuilder();

			if (t.getCaption() == null || t.getCaption().isEmpty())
				sb.append("<Namenlose Therapie>");
			else
				sb.append(t.getCaption());

			String firstDate = (t.getTherapyStart() == null) ? "()" : df.format(t.getTherapyEnd());
			String endDate = (t.getTherapyEnd() == null) ? "()" : df.format(t.getTherapyEnd());
			return sb.append(" [").append(firstDate).append(" - ").append(endDate).append("]").toString();
		}

		public Object getParent(Object o) {
			return ((Therapy) o).getPatient();
		}

	}

}