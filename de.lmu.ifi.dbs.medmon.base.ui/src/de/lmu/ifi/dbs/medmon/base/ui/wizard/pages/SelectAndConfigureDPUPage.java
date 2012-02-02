package de.lmu.ifi.dbs.medmon.base.ui.wizard.pages;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.knowing.core.model.IDataProcessingUnit;
import de.lmu.ifi.dbs.medmon.base.ui.Activator;
import de.lmu.ifi.dbs.medmon.base.ui.filter.DPUInputFilter;
import de.lmu.ifi.dbs.medmon.base.ui.filter.DPUSearchFilter;
import de.lmu.ifi.dbs.medmon.base.ui.viewer.DPUTableViewer;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.IValidationPage;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.ValidationListener;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

public class SelectAndConfigureDPUPage extends WizardPage implements IValidationPage {

	private final Logger		log						= LoggerFactory.getLogger(Activator.PLUGIN_ID);

	private Text				textSearch;
	private DPUTableViewer		dpuViewer;
	private IDataProcessingUnit	selectedDPU;
	private SortedSet<String>	errors					= new TreeSet<String>();
	private Table				table;

	private String				ERROR_NO_DPU_SELECTED	= "Kein Algorithmus ausgewählt";

	/**
	 * Create the wizard.
	 */
	public SelectAndConfigureDPUPage() {
		super("dpu-configuration");
		setTitle("Klassifikationsverfahren");
		setDescription("Klassifikationsverfahren auswaehlen");
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

		Label lableSearch = new Label(container, SWT.NONE);
		lableSearch.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lableSearch.setText("Suche");

		// Search filter
		final DPUSearchFilter dpuSearchFilter = new DPUSearchFilter();

		// Filters all loader
		final DPUInputFilter dpuInputFilter = new DPUInputFilter();

		// Filters all saver

		textSearch = new Text(container, SWT.BORDER);
		textSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textSearch.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				dpuSearchFilter.setSearchText(textSearch.getText());
				dpuViewer.refresh();
			}
		});

		dpuViewer = new DPUTableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		dpuViewer.addFilter(dpuSearchFilter);
		dpuViewer.addFilter(dpuInputFilter);
		table = dpuViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		table.addSelectionListener(new ValidationListener(this) {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) dpuViewer.getSelection();
				if (!selection.isEmpty())
					selectedDPU = (IDataProcessingUnit) selection.getFirstElement();
				else
					selectedDPU = null;
				super.widgetSelected(e);
			}
		});

		initialize();
	}

	private void initialize() {

		if (table.getItemCount() > 0) {
			table.setSelection(0);
			IStructuredSelection selection = (IStructuredSelection) dpuViewer.getSelection();
			selectedDPU = (IDataProcessingUnit) selection.getFirstElement();
		}

		checkContents();

	}

	@Override
	public void checkContents() {

		if (selectedDPU == null)
			errors.add(ERROR_NO_DPU_SELECTED);
		else
			errors.remove(ERROR_NO_DPU_SELECTED);

		if (errors.isEmpty()) {
			setErrorMessage(null);
			setPageComplete(true);
		} else {
			setErrorMessage(errors.first());
			setPageComplete(false);
		}
	}

	/**
	 * @return selected DPU or null
	 */
	public IDataProcessingUnit getDataProcessingUnit() {
		IStructuredSelection selection = (IStructuredSelection) dpuViewer.getSelection();
		if (selection.isEmpty())
			return null;
		return (IDataProcessingUnit) selection.getFirstElement();
	}
}
