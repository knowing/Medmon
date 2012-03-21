package de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.base.ui.viewer.SensorTableViewer;
import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.job.LoadPatientJob;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.provider.PatientProposalProvider;
import de.lmu.ifi.dbs.medmon.medic.ui.provider.TextContentAdapter2;

public class SensorPage extends WizardPage {

	private Button					bPatient;
	private Text					tPatient;
	private TableViewer				sensorViewer;

	private IStructuredSelection	initialSelection;
	private String					initialPatient;

	private boolean					flip	= false;

	private static final Logger		log		= LoggerFactory.getLogger(Activator.PLUGIN_ID);

	/**
	 * Create the wizard.
	 */
	public SensorPage() {
		super("Data");
		setTitle("Daten");
		setDescription("Daten auswaehlen");
	}

	/*
	 * public SensorPage(Patient patient, Object sensor) {
	 * log.debug("SensorPage::SensorPage()");
	 * 
	 * this(); initialPatient = PatientProposalProvider.parseString(patient);
	 * initialSelection = new StructuredSelection(sensor);
	 * 
	 * }
	 */
	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(2, false));

		PageController controller = new PageController();
		tPatient = new Text(container, SWT.BORDER);
		GridData data = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		data.widthHint = 170;
		tPatient.setLayoutData(data);
		if (initialPatient != null)
			tPatient.setText(initialPatient);
		createContentAssistent(tPatient);

		bPatient = new Button(container, SWT.NONE);
		bPatient.setText("Patient auswaehlen");
		bPatient.addListener(SWT.Selection, controller);

		sensorViewer = new SensorTableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		Table table = sensorViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		table.addListener(SWT.Selection, controller);

		setPageComplete(true);
	}

	@Override
	public boolean canFlipToNextPage() {
		return flip;
	}

	private void createContentAssistent(Text text) {
		ControlDecoration deco = new ControlDecoration(text, SWT.LEFT);
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
			ContentProposalAdapter adapter = new ContentProposalAdapter(text, new TextContentAdapter2(), new PatientProposalProvider(),
					keyStroke, autoActivationCharacters);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/*
	 * public ISensorDataContainer importData() { // Use Sample begin and end
	 * try { getContainer().run(false, false, new IRunnableWithProgress() {
	 * 
	 * @Override public void run(IProgressMonitor monitor) throws
	 * InvocationTargetException, InterruptedException {
	 * monitor.beginTask("Daten laden", 0); try { data = getSensor().getData();
	 * } catch (IOException e) { e.printStackTrace();
	 * MessageDialog.openError(getShell(), "Fehler beim Import",
	 * e.getMessage()); } } });
	 * 
	 * } catch (InvocationTargetException e) { e.printStackTrace(); } catch
	 * (InterruptedException e) { e.printStackTrace(); } return data; }
	 */

	private void done() {
		log.debug("SensorPage::done()");
		/*
		 * flip = (getPatient() != null) && (getSensor() != null);
		 * setPageComplete(flip);
		 */
	}

	public Patient getPatient() {
		return PatientProposalProvider.parsePatient(tPatient.getText());
	}

	public Object getSensor() {
		log.debug("SensorPage::getSensor()");
		/*
		 * IStructuredSelection selection = (IStructuredSelection)
		 * sensorViewer.getSelection(); if (selection.isEmpty()) return null;
		 * return (SensorAdapter) selection.getFirstElement();
		 */
		return null;
	}

	private class PageController implements Listener {

		@Override
		public void handleEvent(Event event) {
			if (event.type == SWT.Selection) {
				if (event.widget == bPatient) {
					selectPatient();
					done();
				} else if (event.widget == sensorViewer.getTable()) {
					done();
				}
			}
		}

		private void selectPatient() {
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
			dialog.setBlockOnOpen(true);
			dialog.setTitle("Patient auswaehlen");
			dialog.setElements(LoadPatientJob.getPatients());
			if (dialog.open() == Window.OK) {
				// Assuming that there's only one Patient Selection
				Patient patient = (Patient) dialog.getResult()[0];
				tPatient.setText(PatientProposalProvider.parseString(patient));
				done();
			}
		}

	}

}
