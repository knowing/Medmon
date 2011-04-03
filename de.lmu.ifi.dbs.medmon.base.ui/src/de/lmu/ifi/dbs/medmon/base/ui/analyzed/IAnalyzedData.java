package de.lmu.ifi.dbs.medmon.base.ui.analyzed;

import org.eclipse.swt.widgets.Composite;

/**
 * Tries to wrap the possible output of an ISensorDataAlgorithm 
 * and provide it for further processing
 * 
 * @author Nepomuk Seiler
 * @version 1.0
 */
public interface IAnalyzedData {
	
	/**
	 * Normally this method should return a ChartComposite from
	 * JFreeChart which displays the output.
	 * 
	 * @return Composite to show up in some MonitorView
	 */
	public void createContent(Composite parent);
	
	public void dispose();

}
