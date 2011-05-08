package de.lmu.ifi.dbs.medmon.base.ui.wizard.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import de.lmu.ifi.dbs.knowing.core.graph.xml.DataProcessingUnit;
import de.lmu.ifi.dbs.medmon.base.ui.filter.DPUFilter;
import de.lmu.ifi.dbs.medmon.base.ui.filter.DPUInputFilter;
import de.lmu.ifi.dbs.medmon.base.ui.viewer.DPUTableViewer;
import de.lmu.ifi.dbs.medmon.medic.core.sensor.SensorAdapter;

/**
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 08.05.2011
 * 
 */
public class SelectDPUPage extends WizardPage {

	private Text tSearch;
	private DPUTableViewer dpuViewer;

	private SensorAdapter sensor;
	private DPUFilter dpuFilter;
	private DPUInputFilter dpuInputFilter;

	/**
	 * Create the wizard.
	 */
	public SelectDPUPage() {
		super("Analyseverfahren auswaehlen");
		setTitle("Analyseverfahren auswaehlen");
		setDescription("Mit Hilfe der Suche koennen Sie die Verfahren einschraenken");
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(3, false));

		Label lSearch = new Label(container, SWT.NONE);
		lSearch.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lSearch.setText("Suche");

		tSearch = new Text(container, SWT.BORDER);
		tSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tSearch.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				dpuFilter.setSearchText(tSearch.getText());
				dpuViewer.refresh();
			}
		});
		new Label(container, SWT.NONE);

		dpuViewer = new DPUTableViewer(container, SWT.BORDER | SWT.SINGLE);
		Table table = dpuViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 4));
		dpuViewer.addFilter(dpuFilter = new DPUFilter());
		dpuViewer.addFilter(dpuInputFilter = new DPUInputFilter());

		Button bImport = new Button(container, SWT.NONE);
		bImport.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		bImport.setText("Import");

		Button bExport = new Button(container, SWT.NONE);
		bExport.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		bExport.setText("Export");

		Button bDelete = new Button(container, SWT.NONE);
		bDelete.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1));
		bDelete.setText("Loeschen");

		Button bDetail = new Button(container, SWT.NONE);
		bDetail.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1));
		bDetail.setText("Details");
	}
	
	public DataProcessingUnit getSelection() {
		return null;
	}

	/**
	 * @param sensor
	 *            the sensor to set
	 */
	public void setSensor(SensorAdapter sensor) {
		this.sensor = sensor;
		updateFilter();
	}
	
	private void updateFilter() {
		if(sensor == null)
			return;
		String id = sensor.getSensorExtension().getConverter().getId();
		dpuInputFilter.setLoaderIds(new String[] { id });
		dpuViewer.refresh();
	}

}
