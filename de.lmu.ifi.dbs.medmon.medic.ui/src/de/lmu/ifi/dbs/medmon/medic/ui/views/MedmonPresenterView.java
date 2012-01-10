package de.lmu.ifi.dbs.medmon.medic.ui.views;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunTask;
import org.eclipse.birt.report.viewer.utilities.WebViewer;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.ServiceRegistration;

import de.lmu.ifi.dbs.knowing.core.factory.UIFactory;
import de.lmu.ifi.dbs.knowing.core.swt.factory.UIFactories;
import de.lmu.ifi.dbs.medmon.medic.core.service.IReportingService;
import de.lmu.ifi.dbs.medmon.medic.reporting.data.PatientReportData;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;

public class MedmonPresenterView extends ViewPart {

	public static final String				ID			= "de.lmu.ifi.dbs.medmon.medic.ui.views.MedmonPresenterView";	//$NON-NLS-1$
	private UIFactory<Composite>			uiFactory;
	private ServiceRegistration<UIFactory>	uiFactoryRegistration;
	private final FormToolkit				formToolkit	= new FormToolkit(Display.getDefault());
	private Browser							browser;

	public MedmonPresenterView() {
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		browser = new Browser(container, SWT.NONE);

		try {
			URL designURL = new URL("platform:/plugin/de.lmu.ifi.dbs.medmon.medic.reporting/files/patient_test.rptdesign");
			URL schemaURL = new URL("platform:/plugin/de.lmu.ifi.dbs.medmon.medic.reporting/files/patient_test.xsd");
			Path reportDocumentPath = Paths.get(System.getProperty("user.home"), ".medmon", "reporting", "document.rtpdocument");

			IReportingService reportingService = Activator.getReportingService();
			reportingService.renderReport(designURL, schemaURL, new PatientReportData(), null, reportDocumentPath, Activator.class.getClassLoader());

			
			HashMap<String, String> renderParams = new HashMap<String, String>();
			renderParams.put("SERVLET_NAME_KEY", "run");
			renderParams.put("FORMAT_KEY", "html");
			WebViewer.display(reportDocumentPath.toString(), browser, renderParams);

		} catch (IOException e) {
			e.printStackTrace();
		}
				
		uiFactory = UIFactories.newTabUIFactoryInstance(parent, MedmonPresenterView.ID);

		uiFactoryRegistration = Activator.getBundleContext()
				.registerService(UIFactory.class, uiFactory, UIFactories.newServiceProperties());

		createActions();
		initializeToolBar();
		initializeMenu();
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	@Override
	public void dispose() {
		if (uiFactoryRegistration != null)
			uiFactoryRegistration.unregister();
		super.dispose();
	}
}
