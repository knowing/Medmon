package de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.datechooser.DateChooserCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import de.lmu.ifi.dbs.medmon.base.ui.viewer.DataViewer;
import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.database.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.medic.core.sensor.SensorAdapter;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.provider.PatientProposalProvider;
import de.lmu.ifi.dbs.medmon.medic.ui.provider.TextContentAdapter2;

public class SelectDBDataPage extends WizardPage {

	private Text tPatient;
	private DateChooserCombo dateCustomStart;
	private DateChooserCombo dateCustomEnd;
	private TableViewer dataViewer;
	private ComboViewer comboViewer;

	/**
	 * Create the wizard.
	 */
	public SelectDBDataPage() {
		super("SelectDBDataPage");
		setMessage("Waehlt momentan alle Daten fuer jeweiligen Patienten");
		setTitle("Datenbankverwaltung");
		setDescription("---");
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);

		setControl(container);
		GridLayout gl_container = new GridLayout(6, false);
		gl_container.horizontalSpacing = 8;
		container.setLayout(gl_container);

		new Label(container, SWT.NONE).setText("Patient ");

		tPatient = new Text(container, SWT.BORDER);
		tPatient.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));

		ControlDecoration deco = new ControlDecoration(tPatient, SWT.LEFT);
		deco.setDescriptionText("Use CNTL + SPACE to see possible values");
		deco.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION).getImage());
		deco.setShowOnlyOnFocus(false);
		// Help the user with the possible inputs
		// "." and "#" will also activate the content proposals
		char[] autoActivationCharacters = new char[] { '.', '#' };
		KeyStroke keyStroke;
		try {
			//
			keyStroke = KeyStroke.getInstance("Ctrl+Space");
			// assume that myTextControl has already been created in some way
			new ContentProposalAdapter(tPatient, new TextContentAdapter2(), new PatientProposalProvider(), keyStroke,
					autoActivationCharacters);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Label lFrom = new Label(container, SWT.NONE);
		lFrom.setText("Von");

		dateCustomStart = new DateChooserCombo(container, SWT.BORDER | SWT.FLAT);

		Label lTo = new Label(container, SWT.NONE);
		lTo.setText("bis");

		dateCustomEnd = new DateChooserCombo(container, SWT.BORDER | SWT.FLAT);
		dateCustomEnd.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		Label lSensor = new Label(container, SWT.NONE);
		lSensor.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lSensor.setText("Sensor");

		comboViewer = new ComboViewer(container, SWT.NONE);
		Combo combo = comboViewer.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboViewer.setLabelProvider(new WorkbenchLabelProvider());
		comboViewer.setContentProvider(ArrayContentProvider.getInstance());
		comboViewer.setInput(getSensorAdapter());
		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if(event.getSelection().isEmpty())
					return;
				IStructuredSelection selection = ((IStructuredSelection)event.getSelection());
				SensorAdapter adapter = (SensorAdapter) selection.getFirstElement();
				dataViewer.setInput(getSensorData(adapter));
			}
		});

		dataViewer = new DataViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		Table table = dataViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1));

		Button bRefresh = new Button(container, SWT.PUSH);
		bRefresh.setText("Aktualisieren");
		bRefresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) comboViewer.getSelection();
				if(selection.isEmpty())
					return;
				SensorAdapter adapter = (SensorAdapter) selection.getFirstElement();
				dataViewer.setInput(getSensorData(adapter));
			}
		});
		
		Composite buttonPanel = new Composite(container, SWT.NONE);
		RowLayout rl_buttonPanel = new RowLayout(SWT.HORIZONTAL);
		buttonPanel.setLayout(rl_buttonPanel);
		buttonPanel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 5, 1));

		Button bDelete = new Button(buttonPanel, SWT.NONE);
		bDelete.setLayoutData(new RowData(70, SWT.DEFAULT));
		bDelete.setText("Loeschen");

		Button bExport = new Button(buttonPanel, SWT.NONE);
		bExport.setLayoutData(new RowData(70, SWT.DEFAULT));
		bExport.setText("Export");

	}

	public Patient getPatient() {
		return PatientProposalProvider.parsePatient(tPatient.getText());
	}

	private List<SensorAdapter> getSensorAdapter() {
		Map<String, SensorAdapter> model = Activator.getSensorService().getSensorAdapters();
		return new ArrayList<SensorAdapter>(model.values());
	}

	private List<Data> getSensorData(SensorAdapter adapter) {
		EntityManager em = JPAUtil.createEntityManager();
		em.getTransaction().begin();
		return em.createNamedQuery("Data.findByPatientAndSensor").setParameter("patient", getPatient())
				.setParameter("sensor", adapter.getSensorEntity()).getResultList();
	}
}
