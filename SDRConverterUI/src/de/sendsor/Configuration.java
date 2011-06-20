package de.sendsor;

/**
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 20.06.2011
 *
 */
public class Configuration {

	private boolean relative = false;
	private String output = "csv";
	private String aggregation = SDRConverter.AGGREGATE_AVERAGE;
	private double unit = 1.0;
	private String interval = SDRConverter.INTERVAL_SECOND;
	/**
	 * @return the relative
	 */
	public boolean isRelative() {
		return relative;
	}
	/**
	 * @param relative the relative to set
	 */
	public void setRelative(boolean relative) {
		this.relative = relative;
	}
	/**
	 * @return the output
	 */
	public String getOutput() {
		return output;
	}
	/**
	 * @param output the output to set
	 */
	public void setOutput(String output) {
		this.output = output;
	}
	/**
	 * @return the aggregation
	 */
	public String getAggregation() {
		return aggregation;
	}
	/**
	 * @param aggregation the aggregation to set
	 */
	public void setAggregation(String aggregation) {
		this.aggregation = aggregation;
	}
	/**
	 * @return the unit
	 */
	public double getUnit() {
		return unit;
	}
	/**
	 * @param unit the unit to set
	 */
	public void setUnit(double unit) {
		this.unit = unit;
	}
	/**
	 * @return the interval
	 */
	public String getInterval() {
		return interval;
	}
	/**
	 * @param interval the interval to set
	 */
	public void setInterval(String interval) {
		this.interval = interval;
	}
	@Override
	public String toString() {
		return "Configuration [relative=" + relative + ", output=" + output + ", aggregation=" + aggregation + ", unit=" + unit
				+ ", interval=" + interval + "]";
	}
	
	
	
}
