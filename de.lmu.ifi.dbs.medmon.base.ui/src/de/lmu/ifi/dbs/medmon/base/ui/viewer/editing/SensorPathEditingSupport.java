package de.lmu.ifi.dbs.medmon.base.ui.viewer.editing;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.base.ui.Activator;

public class SensorPathEditingSupport extends EditingSupport {

	private static final Logger log = LoggerFactory.getLogger(Activator.PLUGIN_ID);

	public SensorPathEditingSupport(TableViewer viewer) {
		super(viewer);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return new PathDialogCellEditor(((TableViewer)getViewer()).getTable());
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		log.debug("SensorPathEditingSupport::getValue()");
		return null;
		/*
		String defaultPath = ((SensorAdapter)element).getDefaultPath();
		if(defaultPath == null)
			return "";
		return defaultPath;
		*/
	}

	@Override
	protected void setValue(Object element, Object value) {
		log.debug("SensorPathEditingSupport::setValue()");
		/*
		SensorAdapter adapter = (SensorAdapter)element;
		adapter.setDefaultPath((String) value);
		EntityManager em = JPAUtil.createEntityManager();
		em.getTransaction().begin();
		em.merge(adapter.getSensorEntity());
		em.getTransaction().commit();
		em.close();
		getViewer().refresh();
		//TODO something changed!
		 */
	}

}
