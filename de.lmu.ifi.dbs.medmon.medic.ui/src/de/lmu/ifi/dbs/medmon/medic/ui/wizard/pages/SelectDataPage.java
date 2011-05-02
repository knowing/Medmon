package de.lmu.ifi.dbs.medmon.medic.ui.wizard.pages;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import de.lmu.ifi.dbs.medmon.base.ui.util.ResourceManager;

public class SelectDataPage extends WizardPage {

	private Composite container;

	private TreeViewer treeViewer;
	private Button bImportAll;	
	
	private boolean importAll = true;
	private boolean persist = true;
	private boolean deleteAfter = true;

	private IStructuredSelection initialSelection;

	public SelectDataPage() {
		super("selectDataPage");
		setImageDescriptor(ResourceManager.getPluginImageDescriptor("de.lmu.ifi.dbs.medmon.rcp", "icons/48/gtk-removable.png"));
		setMessage("Die zu analysierenden Daten auswaehlen");
		setTitle("Sensordaten");
		setPageComplete(true);
	}
	
	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);

		setControl(container);
		container.setLayout(new GridLayout(2, false));

		treeViewer = new TreeViewer(container, SWT.BORDER | SWT.MULTI);
		treeViewer.setContentProvider(ArrayContentProvider.getInstance());
		treeViewer.setLabelProvider(new WorkbenchLabelProvider());
		Tree tree = treeViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3));
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {		
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				bImportAll.setSelection(false);
				importAll = false;
			}
		});
		/*if(initialSelection != null && !initialSelection.isEmpty())
			treeViewer.setSelection(initialSelection, true);*/
		
		final Button bToDB = new Button(container, SWT.CHECK);
		bToDB.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		bToDB.setText("In Datenbank speichern");
		bToDB.setSelection(persist);
		bToDB.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				persist = bToDB.getSelection();
			}
		});
		
		bImportAll = new Button(container, SWT.CHECK);
		bImportAll.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		bImportAll.setText("Alles importieren");
		bImportAll.setSelection(importAll);
		bImportAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				importAll = bImportAll.getSelection();
			}
		});
		
		final Button bDeleteAfter = new Button(container, SWT.CHECK);
		bDeleteAfter.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		bDeleteAfter.setText("Daten danach loeschen");
		bDeleteAfter.setSelection(deleteAfter);
		bDeleteAfter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteAfter = bDeleteAfter.getSelection();
			}
		});

		setPageComplete(true);

	}
	
	public void setViewerInput(Object input) {
		treeViewer.setInput(input);
	}

	public boolean isDeleteAfter() {
		return deleteAfter;
	}
	
	public boolean isPersist() {
		return persist;
	}

}
