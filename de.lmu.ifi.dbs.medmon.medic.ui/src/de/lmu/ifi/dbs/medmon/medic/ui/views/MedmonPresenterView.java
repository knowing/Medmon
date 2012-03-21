package de.lmu.ifi.dbs.medmon.medic.ui.views;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.knowing.core.factory.UIFactory;
import de.lmu.ifi.dbs.knowing.core.swt.factory.UIFactories;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;

public class MedmonPresenterView extends ViewPart {

	public static final String				ID			= "de.lmu.ifi.dbs.medmon.medic.ui.views.MedmonPresenterView";	//$NON-NLS-1$
	private static Logger					log			= LoggerFactory.getLogger(ID);

	private UIFactory<Composite>			uiFactory;
	private ServiceRegistration<UIFactory>	uiFactoryRegistration;
	private final FormToolkit				formToolkit	= new FormToolkit(Display.getDefault());
	private Browser							browser;
	private Composite						analyseContent;

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));
		{
			TabFolder tabFolder = new TabFolder(container, SWT.NONE);
			formToolkit.adapt(tabFolder);
			formToolkit.paintBordersFor(tabFolder);
			{
				TabItem tabAnalyse = new TabItem(tabFolder, SWT.NONE);
				tabAnalyse.setText("Analyse");
				{
					analyseContent = new Composite(tabFolder, SWT.NONE);
					tabAnalyse.setControl(analyseContent);
					formToolkit.paintBordersFor(analyseContent);
					analyseContent.setLayout(new FillLayout(SWT.HORIZONTAL));
				}
			}
			{
				TabItem tabReport = new TabItem(tabFolder, SWT.NONE);
				tabReport.setText("Report");

				browser = new Browser(tabFolder, SWT.NONE);
				tabReport.setControl(browser);
				Activator.getReportingService().registerBrowser(browser, "default");
			}
		}

		uiFactory = UIFactories.newTabUIFactoryInstance(analyseContent, MedmonPresenterView.ID);

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
