package de.lmu.ifi.dbs.medmon.reporting;

import java.util.ArrayList;
import java.util.List;

public class Query {

	public List<Person> getResults() {

		List<Person> result = new ArrayList<Person>();

		result.add(new Person("Silly Goose Man"));
		result.add(new Person("Santa Claus"));
		
		return result;
	}
}
