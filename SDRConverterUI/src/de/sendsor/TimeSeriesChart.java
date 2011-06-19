package de.sendsor;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.Dataset;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 19.06.2011
 * 
 */
public class TimeSeriesChart {

	private Dataset dataset;
	private Map<String, TimeSeries> series;
	private JFreeChart chart;

	/**
	 * 
	 */
	public TimeSeriesChart() {
		dataset = createDataset();
		chart = createChart(dataset);
	}

	public ChartPanel getChartPanel() {
		return new ChartPanel(chart);
	}

	public void buildContent(Instances instances) {
		// TODO TimeSeriesPresenter -> Check for right Instances format
		// guessAndSetClassLabel(instances)
		// Attribute classAttr = instances.attribute("class");
		// instances.setClass(classAttr);
		// First buildContent call
		TimeSeriesCollection dataset = (TimeSeriesCollection) this.dataset;
		if (series == null) {
			initSeries(instances);
		} else {
			for (TimeSeries s : series.values()) {
				dataset.removeSeries(s);
			}
		}

		// Fill content
		Enumeration<Instance> enumeration = instances.enumerateInstances();
		int numInst = instances.numInstances();
		int last = 0;
		int i = 0;
		System.out.println("Compute TimeSeries model with " + numInst +	" instances");
		System.out.println("[                    ][0%]");
		while (enumeration.hasMoreElements()) {
			Instance inst = enumeration.nextElement();
			double dateTime = inst.value(instances.attribute(ResultsUtil.ATTRIBUTE_TIMESTAMP()));
			Date date = new Date((long) dateTime);
			// Add value to every corresponing TimeSeries
			for (String name : series.keySet()) {
				TimeSeries s = series.get(name);
				Attribute attribute = instances.attribute(name);
				double value = inst.value(attribute);
				s.add(new Millisecond(date), value);
			}
			 last = printProgress(i, numInst, last);
			i++;
		}
		System.out.println("[....................][100%]");
		for (TimeSeries s : series.values()) {
			dataset.addSeries(s);
		}
		if (chart != null)
			chart.fireChartChanged();

	}

	public void reset() {
		series = null;
		dataset = createDataset();
		chart = createChart(dataset);
	}

	protected JFreeChart createChart(Dataset dataset) {
		chart = ChartFactory.createTimeSeriesChart("Vorschau", "", "", (XYDataset) dataset, false, false, false);
		XYPlot xyplot = chart.getXYPlot();
		xyplot.setDomainCrosshairVisible(true);
		return chart;
	}

	protected Dataset createDataset() {
		return new TimeSeriesCollection();
	}

	/**
	 * @param instances
	 */
	private void initSeries(Instances instances) {
		series = new HashMap<String, TimeSeries>();
		Map<String, Attribute> values = ResultsUtil.findValueAttributesAsJavaMap(instances);
		for (String key : values.keySet()) {
			Attribute attr = values.get(key);
			series.put(attr.name(), new TimeSeries(key));
		}
	}
	
	/**
	 * @return the chart
	 */
	public JFreeChart getChart() {
		return chart;
	}

	private int printProgress(int current, int complete, int last) {
		int percent = (current * 100) / complete;
		int dots = percent / 5;
		boolean print = (percent % 5) == 0;
		if (print && last != dots) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (int i = 0; i < dots; i++)
				sb.append(".");
			for (int i = dots; i < 20; i++)
				sb.append(" ");
			sb.append("][");
			sb.append(percent);
			sb.append("%]");
			System.out.println(sb.toString());
		}
		return dots;
	}
}
