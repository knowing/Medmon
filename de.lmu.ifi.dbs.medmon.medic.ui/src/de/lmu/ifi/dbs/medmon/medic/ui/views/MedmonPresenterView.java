package de.lmu.ifi.dbs.medmon.medic.ui.views;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
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
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

import de.lmu.ifi.dbs.knowing.core.factory.UIFactory;
import de.lmu.ifi.dbs.knowing.core.swt.factory.UIFactories;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.reporting.PatientReportData;

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

		try {
			JAXBContext context = JAXBContext.newInstance(PatientReportData.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			Path reportDataPath = Paths.get(System.getProperty("user.home"), ".medmon", "reporting", "patient_test.xml");
			Path reportSchema = Paths.get(System.getProperty("user.home"), ".medmon", "reporting", "patient_test.xsd");
			Path reportDesignPath = Paths.get(System.getProperty("user.home"), ".medmon", "reporting", "patient_test.rptdesign");
			Path reportDocumentPath = Paths.get(System.getProperty("user.home"), ".medmon", "reporting", "patient_test.rptdocument");
			
			marshaller.marshal(new PatientReportData(), new FileWriter(reportDataPath.toFile()));

			EngineConfig config = new EngineConfig();
			org.eclipse.birt.core.framework.Platform.startup(config);

			IReportEngineFactory factory = (IReportEngineFactory) org.eclipse.birt.core.framework.Platform
					.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
			IReportEngine reportEngine = factory.createReportEngine(config);

			IReportRunnable design = reportEngine.openReportDesign(reportDesignPath.toString());
			IRunTask task = reportEngine.createRunTask(design);
			task.setParameterValue("dataFile", reportDataPath.toString());
			task.setParameterValue("schemaFile", reportSchema.toString());
			task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, Activator.class.getClassLoader());
			task.run(reportDocumentPath.toString());
			task.close();

			browser = new Browser(container, SWT.NONE);
			HashMap<String, String> myparms = new HashMap<String, String>();
			myparms.put("SERVLET_NAME_KEY", "run");
			myparms.put("FORMAT_KEY", "html");
			WebViewer.display(reportDocumentPath.toString(), browser, myparms);

		} catch (JAXBException | IOException | BirtException e) {
			e.printStackTrace();
		}

		// System.out.println("STARTSART");
		// try {
		// EngineConfig config = new EngineConfig();
		// org.eclipse.birt.core.framework.Platform.startup(config);
		//
		// IReportEngineFactory factory = (IReportEngineFactory)
		// org.eclipse.birt.core.framework.Platform
		// .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
		// IReportEngine reportEngine = factory.createReportEngine(config);
		//
		// Bundle bundle = Platform.getBundle("de.lmu.ifi.dbs.medmon.medic.ui");
		// URL url = FileLocator.find(bundle, new Path("patients.rptdesign"),
		// null);
		// String reportName = FileLocator.toFileURL(url).getPath();
		//
		// IReportRunnable design = reportEngine.openReportDesign(reportName);
		// IRunTask task = reportEngine.createRunTask(design);
		// task.setParameterValue("Name", new String("fofofofofofofo"));
		// task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY,
		// Activator.class.getClassLoader());
		// task.run("C:/generated.rptdocument");
		// task.close();
		//
		//
		//
		// browser = new Browser(container, SWT.NONE);
		//
		// HashMap<String, String> myparms = new HashMap<String, String>();
		// myparms.put("SERVLET_NAME_KEY", "run");
		// myparms.put("FORMAT_KEY", "html");
		// WebViewer.display("C:/generated.rptdocument", browser, myparms);
		//
		// } catch (IOException | BirtException e) {
		// System.out.println("Error");
		// e.printStackTrace();
		// }

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
