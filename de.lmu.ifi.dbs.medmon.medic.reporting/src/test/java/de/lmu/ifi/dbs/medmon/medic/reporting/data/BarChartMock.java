package de.lmu.ifi.dbs.medmon.medic.reporting.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import weka.core.DenseInstance;
import weka.core.Instances;
import de.lmu.ifi.dbs.knowing.core.results.TimeIntervalResults;

public class BarChartMock {

	private final Instances instances;

	public BarChartMock() {
		List<String> classes = new ArrayList<>();
		classes.add("Label A");
		classes.add("Label B");
		classes.add("Label C");
		instances = TimeIntervalResults.newInstances(classes);
		fillInstances();
	}
	
	public Instances getInstances() {
		return instances;
	}
	
	private void fillInstances() {
		Calendar cal = Calendar.getInstance();
		
		double[] d1 = new double[3];
		d1[0] = cal.getTimeInMillis();
		cal.add(Calendar.DAY_OF_YEAR, 4);
		d1[1] = cal.getTimeInMillis();
		d1[2] = 0.0; //index of Label A
		instances.add(new DenseInstance(1, d1));
		
		double[] d2 = new double[3];
		cal.add(Calendar.DAY_OF_YEAR, -2);
		d2[0] = cal.getTimeInMillis();
		cal.add(Calendar.DAY_OF_YEAR, 3);
		d2[1] = cal.getTimeInMillis();
		d2[2] = 1.0; //index of Label B
		instances.add(new DenseInstance(1, d2));
		
		double[] d3 = new double[3];
		d3[0] = cal.getTimeInMillis();
		cal.add(Calendar.DAY_OF_YEAR, 5);
		d3[1] = cal.getTimeInMillis();
		d3[2] = 0.0; //index of Label A
		instances.add(new DenseInstance(1, d3));
		
		double[] d4 = new double[3];
		cal.add(Calendar.DAY_OF_YEAR, -1);
		d4[0] = cal.getTimeInMillis();
		cal.add(Calendar.DAY_OF_YEAR, 3);
		d4[1] = cal.getTimeInMillis();
		d4[2] = 2.0; //index of Label C
		instances.add(new DenseInstance(1, d4));
		
		double[] d5 = new double[3];
		d5[0] = cal.getTimeInMillis();
		cal.add(Calendar.DAY_OF_YEAR, 3);
		d5[1] = cal.getTimeInMillis();
		d5[2] = 1.0; //index of Label C
		instances.add(new DenseInstance(1, d5));
		
		double[] d6 = new double[3];
		cal.add(Calendar.DAY_OF_YEAR, -1);
		d6[0] = cal.getTimeInMillis();
		cal.add(Calendar.DAY_OF_YEAR, 7);
		d6[1] = cal.getTimeInMillis();
		d6[2] = 2.0; //index of Label C
		instances.add(new DenseInstance(1, d6));
	}
}
