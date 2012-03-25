package de.lmu.ifi.dbs.medmon.medic.ui.views;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.knowing.core.factory.UIFactory;
import de.lmu.ifi.dbs.knowing.core.swt.factory.UIFactories;
import de.lmu.ifi.dbs.medmon.database.entity.Report;
import de.lmu.ifi.dbs.medmon.database.entity.TherapyResult;
import de.lmu.ifi.dbs.medmon.medic.core.service.GlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionListener;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;

public class MedmonPresenterView extends ViewPart {
	public MedmonPresenterView() {
	}

	public static final String				ID	= "de.lmu.ifi.dbs.medmon.medic.ui.views.MedmonPresenterView";	//$NON-NLS-1$
	private static Logger					log	= LoggerFactory.getLogger(ID);

	private UIFactory<Composite>			uiFactory;
	private ServiceRegistration<UIFactory>	uiFactoryRegistration;
	private TabFolder						tabFolder;
	private TabItem							reportsTab;
	private TabItem							resultsTab;

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		tabFolder = new TabFolder(parent, SWT.BOTTOM);
		toolkit.adapt(tabFolder);
		toolkit.paintBordersFor(tabFolder);

		resultsTab = new TabItem(tabFolder, SWT.NONE);
		resultsTab.setText("Ergebnisse");

		Composite analyzeContent = toolkit.createComposite(tabFolder);
		resultsTab.setControl(analyzeContent);
		analyzeContent.setLayout(new FillLayout());
		uiFactory = UIFactories.newTabUIFactoryInstance(analyzeContent, MedmonPresenterView.ID);
		uiFactoryRegistration = Activator.getBundleContext()
				.registerService(UIFactory.class, uiFactory, UIFactories.newServiceProperties());

		reportsTab = new TabItem(tabFolder, SWT.NONE);
		reportsTab.setText("Berichte");

		final Browser browser = new Browser(tabFolder, SWT.NONE);
		reportsTab.setControl(browser);
		toolkit.adapt(browser);


		IGlobalSelectionProvider provider = GlobalSelectionProvider.newInstance(Activator.getBundleContext());
		provider.registerSelectionListener(new IGlobalSelectionListener<Report>() {

			@Override
			public void selectionChanged(Report report) {
				log.debug("Show Report in MedmonPresenter Browser");
				try (BufferedReader br = Files.newBufferedReader(report.toPath(), Charset.defaultCharset())) {
					StringBuilder sb = new StringBuilder();
					String line = br.readLine();
					while (line != null) {
						sb.append(line);
						line = br.readLine();
					}
					browser.setText(sb.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
				bringBrowserToFront();
			}

			@Override
			public void selectionUpdated() {

			}

			@Override
			public Class<Report> getType() {
				return Report.class;
			}

		});

		provider.registerSelectionListener(new IGlobalSelectionListener<TherapyResult>() {

			@Override
			public void selectionChanged(TherapyResult selection) {
				log.debug("Show TherapyResult in MedmonPresenter UIFactory");
				bringUIFactoryToFront();
			}

			@Override
			public void selectionUpdated() {
			}

			@Override
			public Class<TherapyResult> getType() {
				return TherapyResult.class;
			}

		});

		createActions();
		initializeToolBar();
		initializeMenu();
	}

	private void bringBrowserToFront() {
		tabFolder.setSelection(reportsTab);
	}

	private void bringUIFactoryToFront() {
		tabFolder.setSelection(resultsTab);
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
