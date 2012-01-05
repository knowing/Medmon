package de.lmu.ifi.dbs.medmon.medic.ui.views;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.viewer.ViewerPlugin;
import org.eclipse.birt.report.viewer.utilities.WebViewer;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

import de.lmu.ifi.dbs.knowing.core.factory.UIFactory;
import de.lmu.ifi.dbs.knowing.core.swt.factory.UIFactories;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;

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
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));

		System.out.println("STARTSART");
		try {

			Bundle bundle = Platform.getBundle("de.lmu.ifi.dbs.medmon.reporting");
			URL url = FileLocator.find(bundle, new Path("patients.rptdesign"), null);
			String reportName = FileLocator.toFileURL(url).getPath();
			browser = new Browser(container, SWT.NONE);

			HashMap myparms = new HashMap();
			myparms.put("SERVLET_NAME_KEY", "frameset");
			myparms.put("FORMAT_KEY", "html");
			// myparms.put("RESOURCE_FOLDER_KEY", "c:/myresources");
			// myparms.put("ALLOW_PAGE", false);
			// myparms.put("MAX_ROWS_KEY", "500");
			WebViewer.startup(browser);
			WebViewer.display(reportName, browser, myparms);

		} catch (IOException e) {
			System.out.println("Error");
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
