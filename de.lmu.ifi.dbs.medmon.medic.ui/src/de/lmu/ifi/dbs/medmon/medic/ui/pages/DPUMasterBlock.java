package de.lmu.ifi.dbs.medmon.medic.ui.pages;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.knowing.core.model.IDataProcessingUnit;
import de.lmu.ifi.dbs.medmon.base.ui.filter.DPUSearchFilter;
import de.lmu.ifi.dbs.medmon.base.ui.util.IMedmonSharedImages;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.medic.ui.handler.ImportDPUHandler;
import de.lmu.ifi.dbs.medmon.medic.ui.provider.DPUContentProvider;

public class DPUMasterBlock extends MasterDetailsBlock {
	
	private static final Logger log = LoggerFactory.getLogger(Activator.PLUGIN_ID);
	
	public DPUMasterBlock() {
	}

	private FormToolkit toolkit;
	private Text tSearch;

	private TreeViewer dpuViewer;
	// private ClusterDPUFilter clusterFilter;
	private DPUSearchFilter dpuFilter;
	private DPUDetailsPage dpuDetailsPage;

	/**
	 * Create contents of the master details block.
	 * 
	 * @param managedForm
	 * @param parent
	 */
	@Override
	protected void createMasterPart(final IManagedForm managedForm, Composite parent) {
		toolkit = managedForm.getToolkit();
		//
		Section sDPU = toolkit.createSection(parent, ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR);
		sDPU.setText("Analyseverfahren");
		//
		Composite composite = toolkit.createComposite(sDPU, SWT.NONE);
		sDPU.setClient(composite);
		composite.setLayout(new GridLayout(2, false));

		tSearch = toolkit.createText(composite, "New Text", SWT.SEARCH);
		tSearch.setText("");
		tSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tSearch.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				dpuFilter.setSearchText(tSearch.getText());
				dpuViewer.refresh();
			}
		});

		final Button bCluster = toolkit.createButton(composite, "Cluster", SWT.CHECK);
		bCluster.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// clusterFilter.setCluster(bCluster.getSelection());
				dpuViewer.refresh();
			}
		});

		dpuViewer = new TreeViewer(composite, SWT.BORDER);
		dpuViewer.setContentProvider(new DPUContentProvider());
		dpuViewer.setLabelProvider(new WorkbenchLabelProvider());
		dpuViewer.setInput(getUnits());
		final SectionPart part = new SectionPart(sDPU);
		dpuViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				log.debug("MasterDetailsBlock::createMasterPart -> dpuViewer.addSelectionChangedListener => Activator.getPatientService().setSelection(event.getSelection());");
				managedForm.fireSelectionChanged(part, event.getSelection());
			}
		});
		// clusterFilter = new ClusterDPUFilter();
		dpuFilter = new DPUSearchFilter();
		// dpuViewer.addFilter(clusterFilter);
		dpuViewer.addFilter(dpuFilter);
		Tree tree = dpuViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		toolkit.paintBordersFor(tree);

		Button bRefresh = toolkit.createButton(composite, "aktualisieren", SWT.NONE);
		bRefresh.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		bRefresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dpuViewer.setInput(getUnits());
			}
		});

		Button bImport = toolkit.createButton(composite, "importieren", SWT.NONE);
		bImport.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		bImport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IHandlerService service = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
				try {
					service.executeCommand(ImportDPUHandler.ID, null);
				} catch (Exception ex) {
					new RuntimeException(ImportDPUHandler.ID, ex);
				}
			}
		});
	}

	/**
	 * Register the pages.
	 * 
	 * @param part
	 */
	@Override
	protected void registerPages(DetailsPart part) {
		dpuDetailsPage = new DPUDetailsPage();
		part.registerPage(IDataProcessingUnit.class, dpuDetailsPage);
	}

	/**
	 * Create the toolbar actions.
	 * 
	 * @param managedForm
	 */
	@Override
	protected void createToolBarActions(IManagedForm managedForm) {
		IToolBarManager toolBarManager = managedForm.getForm().getToolBarManager();
		toolBarManager.add(new Action("speichern", PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT)) {

			@Override
			public void run() {
				dpuDetailsPage.commit(true);
			}
		});
	}

	private IDataProcessingUnit[] getUnits() {
		File dpu_dir = new File(IMedmonSharedImages.DIR_DPU);
		String[] files = dpu_dir.list();
		String path = IMedmonSharedImages.DIR_DPU + IMedmonSharedImages.DIR_SEPERATOR;
		IDataProcessingUnit[] returns = new IDataProcessingUnit[files.length];

		for (int i = 0; i < returns.length; i++) {
			
			try {
				XmlResourceStore store = new XmlResourceStore(new File(path + files[i]));
				RootXmlResource resource = new RootXmlResource(store);
				IDataProcessingUnit dpu = IDataProcessingUnit.TYPE.instantiate(resource);
				returns[i] = dpu;
			} catch (ResourceStoreException e) {
				e.printStackTrace();
			}
		}
		return returns;
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		dpuViewer.addSelectionChangedListener(listener);
	}

}
