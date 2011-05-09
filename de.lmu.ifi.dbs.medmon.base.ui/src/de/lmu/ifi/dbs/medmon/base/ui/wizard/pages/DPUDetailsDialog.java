package de.lmu.ifi.dbs.medmon.base.ui.wizard.pages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ListViewer;

import de.lmu.ifi.dbs.knowing.core.graph.xml.*;

/**
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 09.05.2011
 *
 */
public class DPUDetailsDialog extends Dialog {
	
	private Text tName;
	private Text tTags;
	private Text tDescription;
	private ListViewer nodesViewer;
	
	private final DataProcessingUnit dpu;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public DPUDetailsDialog(Shell parentShell, DataProcessingUnit dpu) {
		super(parentShell);
		this.dpu = dpu;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 10;
		layout.verticalSpacing = 15;
		layout.marginTop = 5;
		container.setLayout(layout);
		
		Label lName = new Label(container, SWT.NONE);
		lName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lName.setText("Name");
		
		tName = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		tName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lTags = new Label(container, SWT.NONE);
		lTags.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lTags.setText("Tags");
		
		tTags = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		tTags.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Group gDescription = new Group(container, SWT.NONE);
		FillLayout fl_gDescription = new FillLayout(SWT.HORIZONTAL);
		fl_gDescription.spacing = 5;
		fl_gDescription.marginWidth = 5;
		fl_gDescription.marginHeight = 5;
		gDescription.setLayout(fl_gDescription);
		GridData gd_gDescription = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd_gDescription.minimumHeight = 100;
		gDescription.setLayoutData(gd_gDescription);
		gDescription.setText("Beschreibung");
		
		tDescription = new Text(gDescription, SWT.READ_ONLY | SWT.V_SCROLL);
		
		Group gInput = new Group(container, SWT.NONE);
		FillLayout fl_gInput = new FillLayout(SWT.HORIZONTAL);
		fl_gInput.marginWidth = 5;
		fl_gInput.spacing = 5;
		fl_gInput.marginHeight = 5;
		gInput.setLayout(fl_gInput);
		GridData gd_gInput = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd_gInput.minimumWidth = 100;
		gInput.setLayoutData(gd_gInput);
		gInput.setText("Datenquellen");
		
		nodesViewer = new ListViewer(gInput, SWT.V_SCROLL);
		nodesViewer.setContentProvider(ArrayContentProvider.getInstance());
		nodesViewer.setLabelProvider(new WorkbenchLabelProvider());
		createContent();
		return container;
	}
	
	private void createContent() {
		if(dpu == null)
			return;
		tName.setText(dpu.name());
		tTags.setText(dpu.tags());
		tDescription.setText(dpu.description());
		nodesViewer.setInput(dpu.loaderNodes());
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 700);
	}
}
