package de.lmu.ifi.dbs.medmon.base.ui.wizard.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 01.05.2011
 *
 */
public class SelectDataPage extends WizardPage {

	/**
	 * Create the wizard.
	 */
	public SelectDataPage() {
		super("wizardPage");
		setTitle("Wizard Page title");
		setDescription("Wizard Page description");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		RowLayout rl_container = new RowLayout(SWT.VERTICAL);
		rl_container.fill = true;
		container.setLayout(rl_container);
		
		Button bLatestData = new Button(container, SWT.RADIO);
		bLatestData.setText("Neueste Sensordaten");
		
		Button btnRadioButton_1 = new Button(container, SWT.RADIO);
		btnRadioButton_1.setText("Sensordaten im Zeitraum");
		
		Group grpZeitraum = new Group(container, SWT.NONE);
		grpZeitraum.setText("Zeitraum");
		grpZeitraum.setLayout(new GridLayout(2, false));
		
		Label lFrom = new Label(grpZeitraum, SWT.NONE);
		lFrom.setText("Von: ");
		
		CDateTime dateTime = new CDateTime(grpZeitraum, CDT.CLOCK_24_HOUR | CDT.DATE_SHORT | CDT.TIME_SHORT);
		
		Label lTo = new Label(grpZeitraum, SWT.NONE);
		lTo.setText("Bis: ");
		
		CDateTime dateTime_1 = new CDateTime(grpZeitraum, CDT.TAB_FIELDS | CDT.CLOCK_24_HOUR | CDT.DATE_SHORT | CDT.TIME_SHORT);
		
		Group grpSensorinformation = new Group(container, SWT.NONE);
		grpSensorinformation.setText("Sensor-Information");
	}

}
