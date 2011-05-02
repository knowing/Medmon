package de.lmu.ifi.dbs.medmon.medic.ui.handler;

import java.io.File;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import de.lmu.ifi.dbs.knowing.core.graph.GraphValidator;
import de.lmu.ifi.dbs.knowing.core.graph.xml.DataProcessingUnit;
import de.lmu.ifi.dbs.medmon.medic.core.util.IMedmonConstants;

public class ImportDPUHandler extends AbstractHandler implements IHandler {

	public static final String ID = "de.lmu.ifi.dbs.medmon.medic.ui.importDPU";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setFilterExtensions(new String[] { "*.xml" });
		dialog.setFilterNames(new String[] { "Data Processing Unit" });
		String dpufile = dialog.open();
		try {
			JAXBContext context = JAXBContext.newInstance(DataProcessingUnit.class);
			Unmarshaller um = context.createUnmarshaller();
			DataProcessingUnit dpu = (DataProcessingUnit) um.unmarshal(new File(dpufile));
			GraphValidator validator = new GraphValidator(dpu);
			//Validation
			if(validator.validate()) {
				File file = new File(IMedmonConstants.DIR_DPU + IMedmonConstants.DIR_SEPERATOR + dpu.name());
				if(file.exists()) {
					if(!MessageDialog.openConfirm(shell, "Ueberschreiben?", "Verfahren existiert bereits! Wollen Sie es ueberschreiben?"))
						return null;
					save(dpu, file, context);
					MessageDialog.openInformation(shell, "Import erfolgreich", dpu.name() + " wurde nach importiert");
				} else {
					save(dpu, file, context);
					MessageDialog.openInformation(shell, "Import erfolgreich", dpu.name() + " wurde nach importiert");
				}
			} else {
				Status status = new Status(IStatus.ERROR, "Medic Plugin", createErrorList(validator.getErrors()));
				ErrorDialog errorDialog = new ErrorDialog(shell, "Fehler beim Import", "Die Datei ist nicht korrekt", status, IStatus.ERROR);
				errorDialog.open();
				return null;
			}
		} catch (JAXBException e) {
			e.printStackTrace();
			Status status = new Status(IStatus.ERROR, "Medic Plugin", e.getMessage(), e);
			ErrorDialog errorDialog = new ErrorDialog(shell, "Fehler beim Import", "Die Datei ist nicht korrekt", status, IStatus.ERROR);
			errorDialog.open();
		}
		return null;
	}
	
	private void save(DataProcessingUnit dpu, File file, JAXBContext context) throws JAXBException {
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(dpu, file);
	}
		
	private String createErrorList(String[] errors) {
		StringBuilder sb = new StringBuilder();
		for (String error : errors) {
			sb.append(error);
			sb.append("\n");
		}
		return sb.toString();
	}

}
