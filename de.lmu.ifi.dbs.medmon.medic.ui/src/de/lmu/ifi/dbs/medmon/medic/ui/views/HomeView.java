package de.lmu.ifi.dbs.medmon.medic.ui.views;

import javax.persistence.EntityManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionListener;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.handler.ImportWizardHandler;
import de.lmu.ifi.dbs.medmon.medic.ui.handler.NewPatientHandler;
import de.lmu.ifi.dbs.medmon.medic.ui.provider.ISharedImages;

public class HomeView extends ViewPart {

	private FormToolkit		toolkit;
	private Link			linkOpenLastPatient;
	private EntityManager	workerEM;

	public HomeView() {
		workerEM = JPAUtil.createEntityManager();
	}

	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		ScrolledForm form = toolkit.createScrolledForm(parent);
		// form.setBackgroundImage(Activator.getImageDescriptor("medmon.trans.banner.png").createImage());

		form.setText("Willkommen bei Medmon");
		form.getBody().setLayout(new GridLayout());
		Composite container = toolkit.createComposite(form.getBody());
		GridLayout gl_container = new GridLayout(2, false);
		gl_container.verticalSpacing = 40;
		gl_container.horizontalSpacing = 40;
		container.setLayout(gl_container);
		container.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, true, true));

		ImageHyperlink sensor = toolkit.createImageHyperlink(container, SWT.NONE);
		sensor.setText("Import und Analyse");
		sensor.setImage(Activator.getImageDescriptor(ISharedImages.IMG_IMPORT_48).createImage());
		sensor.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
				try {
					handlerService.executeCommand(ImportWizardHandler.ID, null);
				} catch (Exception ex) {
					throw new RuntimeException(ImportWizardHandler.ID, ex);
				}
			}
		});

		Composite composite = new Composite(container, SWT.NONE);
		toolkit.adapt(composite);
		toolkit.paintBordersFor(composite);
		GridLayout gl_composite = new GridLayout(1, false);
		gl_composite.horizontalSpacing = 0;
		gl_composite.marginHeight = 0;
		composite.setLayout(gl_composite);

		Link link = new Link(composite, SWT.NONE);
		toolkit.adapt(link, true, true);
		link.setText("<a>import und Analyse von Sensor X</a>");

		Link link_1 = new Link(composite, SWT.NONE);
		toolkit.adapt(link_1, true, true);
		link_1.setText("<a>import von Sensor X</a>");

		ImageHyperlink analyse = toolkit.createImageHyperlink(container, SWT.NONE);
		analyse.setText("Patientenverwaltung");
		analyse.setImage(Activator.getImageDescriptor(ISharedImages.IMG_VISUALIZE_48).createImage());
		analyse.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				try {
					IWorkbench workbench = PlatformUI.getWorkbench();
					workbench.showPerspective("de.lmu.ifi.dbs.medmon.medic.ui.default", workbench.getActiveWorkbenchWindow());
				} catch (WorkbenchException e1) {
					e1.printStackTrace();
				}
			}
		});

		linkOpenLastPatient = new Link(container, SWT.NONE);
		toolkit.adapt(linkOpenLastPatient, true, true);
		linkOpenLastPatient.setText("<a>letzten Patienten �ffnen</a>");
		linkOpenLastPatient.setEnabled(false);
		linkOpenLastPatient.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					PlatformUI.getWorkbench().showPerspective("de.lmu.ifi.dbs.medmon.medic.ui.default",
							PlatformUI.getWorkbench().getActiveWorkbenchWindow());
				} catch (WorkbenchException e1) {
					e1.printStackTrace();
				}
			}
		});

		IGlobalSelectionProvider selectionProvider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
		selectionProvider.registerSelectionListener(new IGlobalSelectionListener<Patient>() {
			@Override
			public void selectionChanged(Patient selection) {
				refreshPatientLink(selection);
			}

			@Override
			public void selectionUpdated() {
			}

			@Override
			public Class<Patient> getType() {
				return Patient.class;
			}
		});

		ImageHyperlink patient = toolkit.createImageHyperlink(container, SWT.NONE);
		patient.setText("Patient Anlegen");
		patient.setImage(Activator.getImageDescriptor(ISharedImages.IMG_ADD_PATIENT_48).createImage());
		new Label(container, SWT.NONE);
		patient.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
				try {
					handlerService.executeCommand(NewPatientHandler.ID, null);
				} catch (Exception ex) {
					throw new RuntimeException(NewPatientHandler.ID, ex);
				}
			}
		});

		Label label = toolkit.createLabel(form.getBody(), "");
		label.setImage(Activator.getImageDescriptor("icons/medmon.trans.png").createImage());
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 1, 1));

	}

	private void refreshPatientLink(Patient selection) {
		if (selection == null) {
			linkOpenLastPatient.setText("<a>letzten Patienten �ffnen</a>");
			linkOpenLastPatient.setEnabled(false);
			return;
		}

		selection = workerEM.find(Patient.class, selection.getId());
		workerEM.clear();

		linkOpenLastPatient.setText("<a>" + selection.getLastname() + " " + selection.getFirstname() + " �ffnen</a>");
		linkOpenLastPatient.setEnabled(true);
	};

	@Override
	public void setFocus() {

	}

	@Override
	public void dispose() {
		workerEM.close();
		super.dispose();
	}

}
