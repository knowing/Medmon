package de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.jface.viewers.TableViewerColumn;
import de.lmu.ifi.dbs.medmon.base.ui.viewer.SensorTableViewer;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportDataWizard;
import de.lmu.ifi.dbs.medmon.medic.ui.wizard.ImportWizard;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

public class ImportDataSensorPage extends WizardPage {
	private Table	table;

	/**
	 * Create the wizard.
	 */
	public ImportDataSensorPage() {
		super("wizardPage");
		setTitle("Sensor ausw\u00E4hlen");
		setDescription("<missing>");
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		setPageComplete(false);
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		container.setLayout(new GridLayout(1, false));

		final SensorTableViewer sensorTableViewer = new SensorTableViewer(container, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
		table = sensorTableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ImportDataWizard wizard = (ImportDataWizard) getWizard();
				IStructuredSelection selection = (IStructuredSelection) sensorTableViewer.getSelection();
				if (selection.isEmpty()) {
					setPageComplete(false);
				} else {
					wizard.setSelectedSensor((ISensor) selection.getFirstElement());
					setPageComplete(true);
				}
			}
		});

	}
}
