package de.lmu.ifi.dbs.medmon.medic.ui.reporting;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "TestReportData")
public class PatientReportData {

	@XmlElement
	public String	firstName;
	@XmlElement
	public String	lastName;

	public PatientReportData() {
		firstName = "Silly";
		lastName = "Goose Man";
	}

}
