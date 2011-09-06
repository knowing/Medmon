package de.lmu.ifi.dbs.medmon.base.ui.viewer;

import static de.lmu.ifi.dbs.medmon.medic.core.util.ApplicationConfigurationUtil.getPreferenceStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import de.lmu.ifi.dbs.knowing.core.model.IDataProcessingUnit;
import de.lmu.ifi.dbs.medmon.base.ui.provider.WorkbenchTableLabelProvider;
import de.lmu.ifi.dbs.medmon.medic.core.preferences.IMedicPreferences;

/**
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 08.05.2011
 * 
 */
public class DPUTableViewer extends TableViewer {

	private static final String[] columns = new String[] { "Name", "Beschreibung", "Tags", "Input" };
	private static final int[] width = new int[] { 120, 200, 100, 100 };

	/**
	 * @param parent
	 * @param style
	 */
	public DPUTableViewer(Composite parent, int style) {
		super(parent, style);
		init();
	}

	/**
	 * 
	 * @param table
	 */
	public DPUTableViewer(Table table) {
		super(table);
		init();
	}

	private void init() {
		initColumns();
		initProvider();
		initInput();
	}

	private void initColumns() {
		getTable().setHeaderVisible(true);
		getTable().setLinesVisible(true);
		for (int i = 0; i < columns.length; i++) {
			TableViewerColumn viewerColumn = new TableViewerColumn(this, SWT.LEAD);
			// Kopfzeile und Breite der jeweiligen Spalten festlegen
			viewerColumn.getColumn().setText(columns[i]);
			viewerColumn.getColumn().setWidth(width[i]);
			// Spaltengroesse laesst sich zur Laufzeit aendern
			viewerColumn.getColumn().setResizable(true);
			// Spalten lassen sich untereinander verschieben
			viewerColumn.getColumn().setMoveable(true);
		}
	}

	private void initProvider() {
		setContentProvider(new ArrayContentProvider());
		setLabelProvider(new WorkbenchTableLabelProvider());
	}

	private void initInput() {
		String path = getPreferenceStore().getString(IMedicPreferences.DIR_DPU_ID);
		File dir = new File(path);
		File[] files = dir.listFiles();
		List<IDataProcessingUnit> dpus = new ArrayList<IDataProcessingUnit>();
		for (File file : files) {
			try {
				XmlResourceStore store = new XmlResourceStore(file);
				RootXmlResource resource = new RootXmlResource(store);
				dpus.add((IDataProcessingUnit) IDataProcessingUnit.TYPE.instantiate(resource));
			} catch (ResourceStoreException e) {
				e.printStackTrace();
			}

		}
		setInput(dpus);
	}
}
