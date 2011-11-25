package de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import de.lmu.ifi.dbs.medmon.base.ui.viewer.SensorTableViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class CreateTherapyResultPage extends WizardPage {
	private Text text;
	private Text text_1;
	private Table tableData;
	private CDateTime dateStart;
	private CDateTime dateEnd;

	/**
	 * Create the wizard.
	 */
	public CreateTherapyResultPage() {
		super("CreateTherapyResultPage1");
		setTitle("Therapy-Ergebnis erstellen");
		setDescription("Wählen sie bitte einen Sensor und danach die gewünschten Daten des Sensors aus");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		container.setLayout(new GridLayout(4, false));
		
		Label lblVorname = new Label(container, SWT.NONE);
		lblVorname.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblVorname.setText("Vorname:");
		
		text = new Text(container, SWT.BORDER);
		text.setEditable(false);
		GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_text.widthHint = 150;
		text.setLayoutData(gd_text);
		
		Label label_2 = new Label(container, SWT.NONE);
		label_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_2.setText("Nachname");
		
		text_1 = new Text(container, SWT.BORDER);
		text_1.setEditable(false);
		GridData gd_text_1 = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_text_1.widthHint = 150;
		text_1.setLayoutData(gd_text_1);
		
		Label lblSensor = new Label(container, SWT.NONE);
		lblSensor.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSensor.setText("Sensor:");
		
		Combo combo = new Combo(container, SWT.READ_ONLY);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		Label label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		
		Button btnSelectLatestData = new Button(container, SWT.RADIO);
		btnSelectLatestData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tableData.setEnabled(false);
				dateStart.setEnabled(false);
				dateEnd.setEnabled(false);
			}
		});
		btnSelectLatestData.setSelection(true);
		btnSelectLatestData.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		btnSelectLatestData.setText("Neue Daten importieren");
		
		Label label_1 = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		
		Button btnSelectData = new Button(container, SWT.RADIO);
		btnSelectData.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		btnSelectData.setText("Daten direkt ausw\u00E4hlen");
		btnSelectData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tableData.setEnabled(true);
				dateStart.setEnabled(false);
				dateEnd.setEnabled(false);
			}
		});
		
		SensorTableViewer sensorTableViewer = new SensorTableViewer(container, SWT.BORDER | SWT.FULL_SELECTION, (IStructuredSelection) null);
		tableData = sensorTableViewer.getTable();
		tableData.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 4, 1));
		tableData.setEnabled(false);
		
		Label label_3 = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		
		Button btnSelectiInterval = new Button(container, SWT.RADIO);
		btnSelectiInterval.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		btnSelectiInterval.setText("Intervall ausw\u00E4hlen");
		btnSelectiInterval.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tableData.setEnabled(false);
				dateStart.setEnabled(true);
				dateEnd.setEnabled(true);
			}
		});
		
		Label lblBegin = new Label(container, SWT.NONE);
		lblBegin.setText("Von:");
		
		dateStart = new CDateTime(container, CDT.BORDER | CDT.SPINNER);
		dateStart.setEnabled(false);
		
		Label lblEnd = new Label(container, SWT.NONE);
		lblEnd.setText("Bis:");
		
		dateEnd = new CDateTime(container, CDT.BORDER | CDT.SPINNER);
		dateEnd.setEnabled(false);
		
		
		
	}

}
