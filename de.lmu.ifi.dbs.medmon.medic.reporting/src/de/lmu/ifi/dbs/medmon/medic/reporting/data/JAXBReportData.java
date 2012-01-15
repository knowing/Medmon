package de.lmu.ifi.dbs.medmon.medic.reporting.data;

import java.nio.file.Path;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public abstract class JAXBReportData implements IJAXBReportData {

	@Override
	public void marshal(Path destPath) {
		try {
			JAXBContext context = JAXBContext.newInstance(PatientReportData.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(this, destPath.toFile());
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

}
