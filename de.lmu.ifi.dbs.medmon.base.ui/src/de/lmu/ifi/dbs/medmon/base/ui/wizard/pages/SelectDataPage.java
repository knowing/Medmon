package de.lmu.ifi.dbs.medmon.base.ui.wizard.pages;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;

import scala.Option;
import akka.actor.ActorRef;
import akka.actor.TypedActor;
import akka.actor.TypedActorFactory;
import de.lmu.ifi.dbs.knowing.core.events.Configure;
import de.lmu.ifi.dbs.knowing.core.events.Register;
import de.lmu.ifi.dbs.knowing.core.events.Start;
import de.lmu.ifi.dbs.knowing.core.events.UIFactoryEvent;
import de.lmu.ifi.dbs.knowing.core.factory.TFactory;
import de.lmu.ifi.dbs.knowing.core.factory.UIFactory;
import de.lmu.ifi.dbs.knowing.core.swt.charts.events.ChartProgressListenerRegister;
import de.lmu.ifi.dbs.knowing.core.util.Util;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.IValidationPage;
import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.medic.core.sensor.SensorAdapter;
import de.lmu.ifi.dbs.medmon.sensor.core.container.IBlock;

/**
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 01.05.2011
 * 
 */
public class SelectDataPage extends WizardPage implements IValidationPage {

	private final DateFormat df = DateFormat.getDateTimeInstance();

	private CDateTime dateTimeFrom, dateTimeTo;
	private Button bLatestData, bTimespanData;
	private Button bToPreview, bFromPreview;
	private Label lBlockFromVal, lBlockToVal, lDataSizeVal;
	private Group gPreview;
	private Text tDatePreview;

	private Patient patient;
	private IBlock block;
	private final boolean validate;

	private SensorAdapter sensor;

	private ActorRef loaderActor,presenterActor;


	/**
	 * Create the wizard.
	 * 
	 * @wbp.parser.constructor
	 */
	public SelectDataPage() {
		this(false);
	}

	public SelectDataPage(boolean validate) {
		super("Sensordaten");
		this.validate = validate;
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
		container.setLayout(new GridLayout(2, false));

		bLatestData = new Button(container, SWT.RADIO);
		bLatestData.setText("Neueste Sensordaten");
		bLatestData.setSelection(true);
		bLatestData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateDateSelectionComponents();
				checkContents();
			}
		});

		Group gSensor = new Group(container, SWT.NONE);
		gSensor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 3));
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

		bTimespanData = new Button(container, SWT.RADIO);
		bTimespanData.setText("Sensordaten im Zeitraum");
		bTimespanData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateDateSelectionComponents();
				if (isImportLatest())
					updateDateTimes();
				checkContents();
			}
		});

		Group gTimespan = new Group(container, SWT.NONE);
		gTimespan.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		gTimespan.setText("Zeitraum");
		gTimespan.setLayout(new GridLayout(3, false));

		Label lFrom = new Label(gTimespan, SWT.NONE);
		lFrom.setText("Von: ");

		dateTimeFrom = new CDateTime(gTimespan, CDT.CLOCK_24_HOUR | CDT.DATE_SHORT | CDT.TIME_SHORT);
		dateTimeFrom.setEnabled(false);
		dateTimeFrom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (getFrom().after(getTo()))
					dateTimeTo.setSelection(new Date(getFrom().getTime()));
				checkContents();
			}
		});

		bFromPreview = new Button(gTimespan, SWT.NONE);
		bFromPreview.setText("aus Vorschau");
		bFromPreview.setEnabled(false);
		bFromPreview.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Date date = selectedDate();
				if (date != null) {
					dateTimeFrom.setSelection(date);
					dateTimeFrom.notifyListeners(SWT.Selection, new Event());
				}

				checkContents();
			}
		});

		Label lTo = new Label(gTimespan, SWT.NONE);
		lTo.setText("Bis: ");

		dateTimeTo = new CDateTime(gTimespan, CDT.TAB_FIELDS | CDT.CLOCK_24_HOUR | CDT.DATE_SHORT | CDT.TIME_SHORT);
		dateTimeTo.setEnabled(false);
		dateTimeTo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (getFrom().after(getTo()))
					dateTimeFrom.setSelection(new Date(getTo().getTime()));
				checkContents();
			}
		});

		bToPreview = new Button(gTimespan, SWT.NONE);
		bToPreview.setText("aus Vorschau");
		bToPreview.setEnabled(false);
		bToPreview.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Date date = selectedDate();
				if (date != null) {
					dateTimeTo.setSelection(date);
					dateTimeTo.notifyListeners(SWT.Selection, new Event());
				}

				checkContents();
			}
		});

		gPreview = new Group(container, SWT.NONE);
		gPreview.setLayout(new FillLayout());
		gPreview.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		gPreview.setText("Vorschau");

		Composite cPreview = new Composite(container, SWT.NONE);
		cPreview.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
		cPreview.setLayout(new GridLayout(2, false));

		Label lDatePreview = new Label(cPreview, SWT.NONE);
		lDatePreview.setText("Datum");

		tDatePreview = new Text(cPreview, SWT.BORDER);
		GridData gd_tDatePreview = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1);
		gd_tDatePreview.widthHint = 130;
		tDatePreview.setLayoutData(gd_tDatePreview);

		Button bPreview = new Button(container, SWT.NONE);
		bPreview.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		bPreview.setText("Vorschau");
		bPreview.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				//Dispose old preview
				disposePreview();
				//Get new actors
				Option loader = Util.getFactoryService("de.sendsor.accelerationSensor.converter.SDRLoader");
				Option presenter = Util.getFactoryService("de.lmu.ifi.dbs.knowing.core.swt.charts.TimeSeriesPresenter");

				TFactory presenterFactory = (TFactory) presenter.get();
				TFactory loaderFactory = (TFactory) loader.get();

				presenterActor = presenterFactory.getInstance().start();
				loaderActor = loaderFactory.getInstance().start();
				UIFactory uiFactory = TypedActor.newInstance(UIFactory.class, new TypedActorFactory() {
					@Override
					public TypedActor create() {
						return new DataPageUIFactory(gPreview);
					}
				});
				//Configure PresenterActor
				presenterActor.sendOneWay(new UIFactoryEvent(uiFactory, null));
				presenterActor.sendOneWay(new ChartProgressListenerRegister(new ChartProgressListener() {
					@Override
					public void chartProgress(ChartProgressEvent event) {
						if (event.getType() != ChartProgressEvent.DRAWING_FINISHED)
							return;
						//Detect mouse clicks and print the date to tDatePreview
						JFreeChart chart = null;
						XYPlot plot = null;
						if (event.getSource() instanceof JFreeChart) {
							chart = (JFreeChart) event.getSource();
							plot = (XYPlot) chart.getPlot();
						} else if (event.getSource() instanceof XYPlot) {
							plot = (XYPlot) event.getSource();
						}
						XYDataset dataset = plot.getDataset();
						double time = plot.getDomainCrosshairValue();
						tDatePreview.setText(df.format(new Date((long) time)));
					}
				}));
				//Get SDR files, assumes that there is a least one and takes the first
				File dir = new File(sensor.getDefaultPath());
				String[] sdrFiles = dir.list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".sdr");
					}
				});
				Properties properties = loaderFactory.createDefaultProperties();
				String sep = System.getProperty("file.separator");
				String path = sensor.getDefaultPath() + sep + sdrFiles[0];
				properties.setProperty("file", path);
				// properties.setProperty("absolute-path", "true");
				Option<String> none = scala.Option.apply(null);
				loaderActor.sendOneWay(new Configure(properties));
				loaderActor.sendOneWay(new Register(presenterActor, none));
				loaderActor.sendOneWay(new Start());
				//TODO SelectDataPage update preview after evaluating preview
				uiFactory.update();
			}
		});

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
		if (sensor == null)
			return;
		this.sensor = sensor;
		try {
			block = sensor.convert();
			updateDateTimes();
		} catch (IOException e) {
			MessageDialog.openError(getShell(), "Fehler beim konvertieren der Sensordaten", e.getMessage());
			e.printStackTrace();
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

	private void disposePreview() {
		if (loaderActor != null)
			loaderActor.stop();
		if (presenterActor != null)
			presenterActor.stop();
		for (Control c : gPreview.getChildren())
			c.dispose();
	}

	private Date selectedDate() {
		String dateString = tDatePreview.getText();
		if (dateString == null || dateString.isEmpty())
			return null;
		try {
			return df.parse(dateString);
		} catch (ParseException e1) {
			e1.printStackTrace();
			return null;
		}
	}

	/**
	 * <p>Enabled or disables menu</p>
	 */
	private void updateDateSelectionComponents() {
		boolean enabled = !bLatestData.getSelection();
		dateTimeFrom.setEnabled(enabled);
		dateTimeTo.setEnabled(enabled);
		bToPreview.setEnabled(enabled);
		bFromPreview.setEnabled(enabled);
	}


	@Override
	public void dispose() {
		disposePreview();
		super.dispose();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void checkContents() {
		// No validation
		if (!validate)
			return;
		EntityManager em = JPAUtil.createEntityManager();
		// Check if the identical dataset exists in db
		List<Data> results = em.createNamedQuery("Data.findByPatientAndDate").setParameter("patient", patient)
				.setParameter("from", getFrom()).setParameter("to", getTo()).getResultList();
		if (!results.isEmpty()) {
			setMessage("Datensatz bereits vorhanden", ERROR);
			em.close();
			setPageComplete(false);
			return;
		}

		// Check if the data is new
		results = em.createNamedQuery("Data.findByPatientAndBeforeTo").setParameter("patient", patient).setParameter("date", getFrom())
				.getResultList();
		if (results.isEmpty()) {
			setMessage("Auswahl in Ordnung", INFORMATION);
			em.close();
			setPageComplete(true);
			return;
		}
		// Check if sensor data are older than all datasets in db
		results = em.createNamedQuery("Data.findByPatientAndAfterFrom").setParameter("patient", patient).setParameter("date", getTo())
				.getResultList();
		if (results.isEmpty()) {
			setMessage("Auswahl in Ordnung", INFORMATION);
			em.close();
			setPageComplete(true);
			return;
		}
		// Check if new sensor data fills a gap in the db or overlaps
		results = em.createNamedQuery("Data.findByPatientAndBeforeFrom").setParameter("patient", patient).setParameter("date", getTo())
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
				setPageComplete(true);
				return; // gap found
			}

		}
		// No gap found
		DateFormat df = DateFormat.getDateTimeInstance();
		String fromString = "";
		if (closest != null)
			fromString = df.format(closest);
		setMessage("Datensatz ueberlappt von " + fromString + " bis " + df.format(getFrom()), ERROR);
		em.close();
		setPageComplete(false);
	}

}
