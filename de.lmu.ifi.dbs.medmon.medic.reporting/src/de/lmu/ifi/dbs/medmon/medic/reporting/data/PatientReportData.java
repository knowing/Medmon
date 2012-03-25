package de.lmu.ifi.dbs.medmon.medic.reporting.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "TestReportData")
public class PatientReportData extends JAXBReportData {

	@XmlElement
	public String	firstName;
	@XmlElement
	public String	lastName;

	public PatientReportData() {
		firstName = "Max";
		lastName = "Mustermann";
	}

	@Override
	public String getId() {
		return "data";
	}
}
