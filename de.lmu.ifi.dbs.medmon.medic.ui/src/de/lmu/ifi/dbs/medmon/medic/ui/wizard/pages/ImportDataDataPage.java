package de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import de.lmu.ifi.dbs.medmon.base.ui.viewer.DataViewer;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.IValidationPage;
import de.lmu.ifi.dbs.medmon.base.ui.wizard.ValidationListener;
import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.medic.core.service.ISensorManagerService;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;
import de.lmu.ifi.dbs.medmon.sensor.core.IConverter;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

import org.eclipse.swt.widgets.Label;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.jface.viewers.TableViewerColumn;

public class ImportDataDataPage extends WizardPage implements IValidationPage {

	private URI					selectedURI;
	private ISensor				selectedSensor;

	private SortedSet<String>	errors					= new TreeSet<String>();
	private static String		ERROR_NO_URI_SELECTED	= "Keine Daten ausgewählt";

	private TableViewer			tableViewer;
	private TableViewerColumn	clmViewerFile;
	private TableViewerColumn	clmViewerFrom;
	private TableViewerColumn	clmViewerTo;

	/**
	 * Create the wizard.
	 */
	public ImportDataDataPage() {
		super("wizardPage");
		setTitle("Daten importieren");
		setDescription("<missing>");
	}

	/**
	 * WIZZARD-GET: get selected URI
	 */
	public URI getSelectedURI() {
		return selectedURI;
	}

	/**
	 * WIZZARD-SET: set the input of the DataViewer
	 */
	public void setInput(ISensor sensor, Object input) {
		selectedURI = null;
		selectedSensor = sensor;
		tableViewer.setInput(input);
	}

	/**
	 * WIZZARD-INIT:
	 */
	private void initialize() {
		checkContents();
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		container.setLayout(new GridLayout(1, false));

		tableViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		Table table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		clmViewerFile = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn clmFile = clmViewerFile.getColumn();
		clmFile.setResizable(false);
		clmFile.setWidth(300);
		clmFile.setText("Datei");

		clmViewerFrom = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn clmFrom = clmViewerFrom.getColumn();
		clmFrom.setResizable(false);
		clmFrom.setWidth(120);
		clmFrom.setText("Von");

		clmViewerTo = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn clmTo = clmViewerTo.getColumn();
		clmTo.setResizable(false);
		clmTo.setWidth(120);
		clmTo.setText("Bis");

		table.addSelectionListener(new ValidationListener(this){
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				if (!selection.isEmpty())
					selectedURI = (URI) selection.getFirstElement();
				else
					selectedURI = null;
				super.widgetSelected(e);
			}
		});
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		clmViewerFile.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				cell.setText(((URI) cell.getElement()).toString());
			}
		});
		clmViewerFrom.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
			}
		});
		clmViewerTo.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
			}
		});
		
		initialize();
	}

	@Override
	public void checkContents() {

		if (selectedURI == null)
			errors.add(ERROR_NO_URI_SELECTED);
		else
			errors.remove(ERROR_NO_URI_SELECTED);

		if (errors.isEmpty()) {
			setErrorMessage(null);
			setPageComplete(true);
		} else {
			setErrorMessage(errors.first());
			setPageComplete(false);
		}

	}
}
