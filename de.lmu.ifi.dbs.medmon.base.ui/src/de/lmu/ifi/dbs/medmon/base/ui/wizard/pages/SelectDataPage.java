package de.lmu.ifi.dbs.medmon.base.ui.wizard.pages;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.medic.core.sensor.SensorAdapter;
import de.lmu.ifi.dbs.medmon.sensor.core.container.IBlock;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;

/**
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 01.05.2011
 * 
 */
public class SelectDataPage extends WizardPage {

	private CDateTime dateTimeFrom;
	private CDateTime dateTimeTo;
	private Button bLatestData;
	private Button bTimespanData;
	private Label lBlockFromVal;
	private Label lBlockToVal;
	private Label lDataSizeVal;

	private SensorAdapter sensor;
	private Patient patient;
	private IBlock block;

	/**
	 * Create the wizard.
	 */
	public SelectDataPage() {
		super("Sensordaten");
		setTitle("Sensordaten");
		setDescription("Bitte waehlen Sie aus welche Sensordaten Sie importieren moechten");
		setPageComplete(true);
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		RowLayout layout = new RowLayout(SWT.VERTICAL);
		layout.fill = true;
		container.setLayout(layout);

		bLatestData = new Button(container, SWT.RADIO);
		bLatestData.setText("Neueste Sensordaten");
		bLatestData.setSelection(true);
		bLatestData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dateTimeFrom.setEnabled(!bLatestData.getSelection());
				dateTimeTo.setEnabled(!bLatestData.getSelection());
				setPageComplete(validate());
			}
		});

		bTimespanData = new Button(container, SWT.RADIO);
		bTimespanData.setText("Sensordaten im Zeitraum");
		bTimespanData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dateTimeFrom.setEnabled(!bLatestData.getSelection());
				dateTimeTo.setEnabled(!bLatestData.getSelection());
				if (isImportLatest())
					updateDateTimes();
				setPageComplete(validate());
			}
		});

		Group gTimespan = new Group(container, SWT.NONE);
		gTimespan.setText("Zeitraum");
		gTimespan.setLayout(new GridLayout(2, false));

		Label lFrom = new Label(gTimespan, SWT.NONE);
		lFrom.setText("Von: ");

		dateTimeFrom = new CDateTime(gTimespan, CDT.CLOCK_24_HOUR | CDT.DATE_SHORT | CDT.TIME_SHORT);
		dateTimeFrom.setEnabled(false);
		dateTimeFrom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(getFrom().after(getTo()))
					dateTimeTo.setSelection(new Date(getFrom().getTime()));
				setPageComplete(validate());
			}
		});

		Label lTo = new Label(gTimespan, SWT.NONE);
		lTo.setText("Bis: ");

		dateTimeTo = new CDateTime(gTimespan, CDT.TAB_FIELDS | CDT.CLOCK_24_HOUR | CDT.DATE_SHORT | CDT.TIME_SHORT);
		dateTimeTo.setEnabled(false);
		dateTimeTo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(getFrom().after(getTo()))
					dateTimeFrom.setSelection(new Date(getTo().getTime()));
				setPageComplete(validate());
			}
		});

		Group gSensor = new Group(container, SWT.NONE);
		gSensor.setLayoutData(new RowData(200, SWT.DEFAULT));
		gSensor.setText("Sensor-Information");
		gSensor.setLayout(new GridLayout(2, false));

		Label lBlockFrom = new Label(gSensor, SWT.NONE);
		lBlockFrom.setText("Von");

		lBlockFromVal = new Label(gSensor, SWT.NONE);
		lBlockFromVal.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lBlockFromVal.setText("-");

		Label lBlockTo = new Label(gSensor, SWT.NONE);
		lBlockTo.setText("Bis");

		lBlockToVal = new Label(gSensor, SWT.NONE);
		lBlockToVal.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lBlockToVal.setText("-");

		Label lDataSize = new Label(gSensor, SWT.NONE);
		lDataSize.setText("Datensaetze");

		lDataSizeVal = new Label(gSensor, SWT.NONE);
		lDataSizeVal.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lDataSizeVal.setText("0 MByte");
		setPageComplete(validate());
	}

	public boolean isImportLatest() {
		return bLatestData.getSelection();
	}

	public Date getFrom() {
		return eraseMilliSeconds(dateTimeFrom.getSelection());
	}

	public Date getTo() {
		return eraseMilliSeconds(dateTimeTo.getSelection());
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public void setSensor(SensorAdapter sensor) {
		this.sensor = sensor;
		if (sensor != null) {
			try {
				block = sensor.convert();
				updateDateTimes();
			} catch (IOException e) {
				MessageDialog.openError(getShell(), "Fehler beim konvertieren der Sensordaten", e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private void updateDateTimes() {
		if (block == null)
			return;
		// Set default cdate values
		dateTimeFrom.setSelection(block.getFrom());
		dateTimeTo.setSelection(block.getTo());
		// Set Sensor-Information
		DateFormat df = DateFormat.getDateTimeInstance();
		lBlockFromVal.setText(df.format(block.getFrom()));
		lBlockToVal.setText(df.format(block.getTo()));
		double size = block.getEnd() - block.getBegin();
		size = size / (1024.0 * 1024.0);
		String sizeString = String.valueOf(size);
		if (sizeString.length() > 4)
			sizeString = String.valueOf(size).substring(0, 4);
		lDataSizeVal.setText(sizeString + " MByte");
	}

	private Date eraseMilliSeconds(Date date) {
		if (date == null)
			return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	@SuppressWarnings("unchecked")
	private boolean validate() {
		EntityManager em = JPAUtil.createEntityManager();
		// Check if the identical dataset exists in db
		List<Data> results = em.createNamedQuery("Data.findByPatientAndDate")
				.setParameter("patient", patient)
				.setParameter("from", getFrom())
				.setParameter("to", getTo())
				.getResultList();
		if (!results.isEmpty()) {
			setMessage("Datensatz bereits vorhanden", ERROR);
			em.close();
			return false;
		}

		// Check if the data is new
		results = em.createNamedQuery("Data.findByPatientAndBeforeTo")
				.setParameter("patient", patient)
				.setParameter("date", getFrom())
				.getResultList();
		if (results.isEmpty()) {
			setMessage("Auswahl in Ordnung", INFORMATION);
			em.close();
			return true;
		}
		// Check if sensor data are older than all datasets in db
		results = em.createNamedQuery("Data.findByPatientAndAfterFrom")
				.setParameter("patient", patient)
				.setParameter("date", getTo())
				.getResultList();
		if (results.isEmpty()) {
			setMessage("Auswahl in Ordnung", INFORMATION);
			em.close();
			return true;
		}
		// Check if new sensor data fills a gap in the db or overlaps
		results = em.createNamedQuery("Data.findByPatientAndBeforeFrom")
				.setParameter("patient", patient)
				.setParameter("date", getTo())
				.getResultList();
		
		Date closest = null; // Store closest date for user feedback
		for (Data data : results) {
			if (closest == null)
				closest = data.getTo();
			if (closest.before(data.getTo()))
				closest = data.getTo();

			if (getFrom().after(data.getTo())) {
				setMessage("Auswahl in Ordnung", INFORMATION);
				em.close();
				return true; // gap found
			}

		}
		// No gap found
		DateFormat df = DateFormat.getDateTimeInstance();
		String fromString = "";
		if (closest != null)
			fromString = df.format(closest);
		setMessage("Datensatz ueberlappt von " + fromString + " bis " + df.format(getFrom()), ERROR);
		em.close();
		return false;

	}

	public void initPage() {
		setPageComplete(validate());
	}

}
