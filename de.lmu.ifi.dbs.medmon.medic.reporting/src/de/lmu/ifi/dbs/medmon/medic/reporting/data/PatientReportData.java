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
public class PatientReportData implements IJAXBReportData {

	@XmlElement
	public String	firstName;
	@XmlElement
	public String	lastName;

	public PatientReportData() {
		firstName = "Silly";
		lastName = "Goose Man";
	}

	@Override
	public String getId() {
		return "data";
	}
}
