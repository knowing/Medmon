package de.lmu.ifi.dbs.medmon.datamining.core.processing;

import java.util.Map;
import java.util.Properties;

import weka.core.Capabilities;
import weka.core.Instances;
import weka.core.converters.Loader;
import de.lmu.ifi.dbs.medmon.datamining.core.container.RawData;
import de.lmu.ifi.dbs.medmon.datamining.core.parameter.IProcessorParameter;


public interface IDataProcessor extends Runnable {
	
	public static final String PROCESSOR_ID = "de.lmu.ifi.dbs.medmon.dataminig.core.processor";
	public static final int INDEFINITE_DIMENSION = -1;

	/**
	 * 
	 * @param data
	 * @return
	 */
	public Object process(RawData data);
	
	/**
	 * Current default implementation checks only if dimensions fit.
	 * @param processor
	 * @return
	 */
	public boolean isCompatible(IDataProcessor processor);
	
	public int inputDimension();
	
	public int outputDimension();
		
	/**
	 * If the algorithm has parameters, it should be configured
	 * with a Properties Object and be provided via this method
	 * 
	 * @return configuration properties for the algorithm
	 */
	public Map<String, IProcessorParameter> getParameters();
	
	public IProcessorParameter getParameter(String key);
	
	/**
	 * It's highly recommended to use the parameter's name as a key.
	 * 
	 * @param key (parameter's name)
	 * @param parameter
	 * @return previous value or null if none
	 */
	public IProcessorParameter setParameter(String key, IProcessorParameter parameter);
	
	public String getName();
	
	public String getDescription();
	
	public String getVersion();
	
	public String getId();
	
	/* ==================== */
	/* == XDataProcessor == */
	/* ==================== */
	
	public IDataProcessor getInstance(Properties properties);
	
	public Capabilities getCapabilities();
	
	public void addProcessListener(IProcessListener listener);
	
	public void removeProcessListener(IProcessListener listener);
	
	public boolean isReady();
	
	public boolean isFinished();
	
	public void setLoader(Loader loader);
	
	public Instances getResult();
	
	
}
