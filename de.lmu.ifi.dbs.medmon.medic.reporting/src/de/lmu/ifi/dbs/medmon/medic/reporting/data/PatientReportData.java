package de.lmu.ifi.dbs.medmon.medic.reporting.data;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "TestReportData")
public class PatientReportData implements IReportData {

	@XmlElement
	public String	firstName;
	@XmlElement
	public String	lastName;

	public PatientReportData() {
		firstName = "Silly";
		lastName = "Goose Man";
	}

	@Override
	public void marshal(Path file) {

		try {
			JAXBContext context = JAXBContext.newInstance(PatientReportData.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(new PatientReportData(), file.toFile());
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
