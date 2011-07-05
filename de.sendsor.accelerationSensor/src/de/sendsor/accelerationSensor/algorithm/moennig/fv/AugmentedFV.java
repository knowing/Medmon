package de.sendsor.accelerationSensor.algorithm.moennig.fv;

import java.util.ArrayList;
import java.util.List;

import de.sendsor.accelerationSensor.algorithm.moennig.AverageFilter;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Capabilities.Capability;
import weka.core.MultiInstanceCapabilitiesHandler;
import weka.filters.SimpleBatchFilter;

public class AugmentedFV extends SimpleBatchFilter implements MultiInstanceCapabilitiesHandler{


	private static final long serialVersionUID = 5923685264303443590L;
	
	private int arModelOrder = 3;	
	
	private boolean useAR = false; //use auto regressive coefficients
	private boolean useSMA = false; //use signal magnitude area
	private boolean useTA = false; //use tilt angle
	private boolean useAPA = false; //use average peak amplitude
	private boolean useSSR = false; //use surrounding segmentation rate
	private boolean useMV = false; //use mean and variance
	

	private int relAttIndex; //index of the relational attribute
	private int ssrAttIndex; //index of the surrounding segmentation rate attribute
	private int dimensions; //amount of numeric attributes in the relational attribute (used for building the fv)
	private int numFVattributes; //amount of new fv attributes in the output format
	
	@Override
	protected Instances determineOutputFormat(Instances inputFormat) throws Exception {
		//determine the index of the relational attribute
		relAttIndex = -1;				
		for(int i=0;i<inputFormat.numAttributes() && relAttIndex<0;i++){
			if(inputFormat.attribute(i).isRelationValued()){
				relAttIndex = i;				
			}
		}
		
		//determine the index of the surrounding segmentation rate attribute
		ssrAttIndex = inputFormat.attribute("SSR") != null ? inputFormat.attribute("SSR").index() : -1;
				
		Instances rel = inputFormat.attribute(relAttIndex).relation();
		dimensions = 0;
		for(int i=0;i<rel.numAttributes();i++){
			if(rel.attribute(i).type() == Attribute.NUMERIC && rel.classIndex()!=i){
				dimensions++;
			}
		} 
		
		numFVattributes = 0;
		
		Instances result = new Instances(inputFormat, 0);	     	     
	    if(useAR){	    	
	    	 for(int i=0;i<dimensions;i++){
	    		 for(int j=0;j<arModelOrder;j++){
	    			 result.insertAttributeAt(new Attribute("FV_AR_"+i+"_"+j), result.numAttributes());
	    			 numFVattributes++;
	    		 }
	    	 }	    	 
	    }
	    if(useSMA){
	    	result.insertAttributeAt(new Attribute("FV_SMA"), result.numAttributes());
	    	numFVattributes++;
	    }
	    if(useTA){
	    	result.insertAttributeAt(new Attribute("FV_TA"), result.numAttributes());
	    	numFVattributes++;
	    }
	    if(useAPA){
	    	for(int i=0;i<dimensions;i++){
	    		result.insertAttributeAt(new Attribute("FV_APA_"+i), result.numAttributes());
	    		numFVattributes++;
	    	}
	    }
	    if(useSSR && ssrAttIndex>=0){
	    	result.insertAttributeAt(new Attribute("FV_SSR"), result.numAttributes());
	    	numFVattributes++;
	    }
	    if(useMV){
	    	for(int i=0;i<dimensions;i++){
	    		result.insertAttributeAt(new Attribute("FV_MEAN"), result.numAttributes());
	    		result.insertAttributeAt(new Attribute("FV_VAR"), result.numAttributes());
	    		numFVattributes += 2;
	    	}
	    }
	    
	    	 
	    return result;
	}

	@Override
	public String globalInfo() {
		return "Builds a FV for the numeric values of the relational attribute of the input data";
	}
	
	@Override
	public Capabilities getCapabilities() {
	     Capabilities result = super.getCapabilities();	     
	     result.enable(Capability.DATE_ATTRIBUTES); // date attribute is accepted, but is not processed
	     result.enable(Capability.RELATIONAL_ATTRIBUTES); // only relational attributes are used for processing
	     result.enable(Capability.STRING_CLASS);
	     result.enable(Capability.NO_CLASS);
	     
	     return result;
	}
	
	@Override
	public Capabilities getMultiInstanceCapabilities() {
		Capabilities result = super.getCapabilities();	     
	    result.enable(Capability.DATE_ATTRIBUTES); // date attribute is accepted, but is not processed
	    result.enable(Capability.NUMERIC_ATTRIBUTES); // only numeric attributes are proccessed	    
	     
	    return result;
	}

	@Override
	protected Instances process(Instances input) throws Exception {				
		Instances output = new Instances(determineOutputFormat(input), 0);
		for(int i=0;i<input.numInstances();i++){
			output.add(buildFV(input.get(i), relAttIndex));
		}
		
		return output;
	}
	
	private Instance buildFV(Instance in, int relAttIndex) throws Exception{
		
		Instances data = in.relationalValue(relAttIndex);		
		
		//extract numerical data of the relational attribute
		double[][] numData = new double[dimensions][data.numInstances()];
		for(int i=0;i<data.numInstances();i++){
			int d = 0;
			for(int j=0;j<data.numAttributes();j++){
				if(data.attribute(j).type() == Attribute.NUMERIC && data.classIndex()!=j){
					numData[d][i] = data.get(i).value(j);
					d++;
				}
			}
		}
		
		//initialize the fv array
		double[] fvValues = new double[numFVattributes];
		int fvIndex = 0;
		
		//AR coeffients
		if(useAR){			
			for(int j=0;j<numData.length;j++){
				double[] ar = AutoRegression.calculateARCoefficients(numData[j], arModelOrder, true);													
				for(double c : ar){
					fvValues[fvIndex] = c;
					fvIndex++;
				}								
			}
		}
		
		//Signal Magnitude Area
		if(useSMA){
			fvValues[fvIndex] = calcSignalMagnitudeArea(numData);
			fvIndex++;
		}

		//Tilt Angle
		if(useTA){
			fvValues[fvIndex] = calcTiltAngle(numData);
			fvIndex++;
		}
		
		//Average Peak Amplitude
		if(useAPA){
			for(int j=0;j<numData.length;j++){
				fvValues[fvIndex] = calcAveragePeakAmplitude(numData[j]);
				fvIndex++;
			}
		}
		
		//Surrounding Segmentation Rate
		if(useSSR && ssrAttIndex>=0){
			fvValues[fvIndex] = in.value(ssrAttIndex);
			fvIndex++;
		}		
		
		//Mean & Variance
		if(useMV){
			for(int j=0;j<numData.length;j++){
				double mean = calcMean(numData[j]);
				fvValues[fvIndex] = mean;
				fvIndex++;
				
				fvValues[fvIndex] = calcVariance(numData[j],mean);
				fvIndex++;
			}
		}

		//Copy old values and new fv values into the resulting instance
		Instance r = new DenseInstance(in.numAttributes()+numFVattributes);
		for(int i=0;i<in.numAttributes();i++){			
			r.setValue(i, in.value(i));			
		}
		for(int i=0;i<numFVattributes;i++){
			r.setValue(i+in.numAttributes(), fvValues[i]);
		}
		return r;		
	}
	
	/**
	 * Calculates the signal magnitude area - a commonly used energy measure.
	 * It takes the absolute values of all attributes and sums them up
	 * @param input data to calculate the SMA for
	 * @return the SMA value
	 */
	private double calcSignalMagnitudeArea(double[][] input){
		double sma = 0.0;
		for(int i=0;i<input[0].length;i++){
			for(int d=0;d<input.length;d++){
				sma += Math.abs(input[d][i]);
			}
		}
		//normalize according to input length		
		sma /= (input[0].length);
		return sma;
	}
	
	/**
	 * calculates the mean tilt angle for the given input samples
	 * @param input data for which the mean tilt angle is calculated
	 * @return mean tilt angle in the range of 0.0 and Pi
	 */
	private double calcTiltAngle(double[][] input){
		double[] meanVector = new double[3];		
		for(int i=0;i<input[0].length;i++){
			meanVector[0] += input[0][i];
			meanVector[1] += input[1][i];
			meanVector[2] += input[2][i];			
		}
		double vectorNorm = 0.0;
		for(int i=0;i<meanVector.length;i++){
			vectorNorm += meanVector[i]*meanVector[i];
		}
		vectorNorm = Math.sqrt(vectorNorm);
		double cos = meanVector[2]/vectorNorm;			
		
		return Math.acos(cos);
	}
	
	/**
	 * Calculates the average peak for the given data.
	 * According to the mean of the samples, the negative (if mean is negative)
	 * or positive (if mean is positive) peaks are choosen for calculation 
	 * 
	 * @param window
	 * @return the average peak (positive or negative) or 1.0 respectively -1.0 
	 * as defaul peak if not at least 3 peaks are present.
	 */
	private double calcAveragePeakAmplitude(double[] window){		
		List<Integer> peaks = calcPeaks(window);
		double amplitude = 0.0;		
		if(peaks.size()>=3){
			for(int i=0;i<peaks.size();i++){
				amplitude += window[peaks.get(i)];			
			}
			amplitude /= peaks.size();			
		}
		else{
			if(window[peaks.get(0)]>0){ //consider the avg. peak as 1.0 respectively -1.0 if there aren't at least 3 peaks
				amplitude = 1.0;
			}
			else{
				amplitude = -1.0;
			}
		}
		return amplitude;
	}
	
	/**
	 * Calculates the peaks of the given data. 
	 * According to the mean of the data, the negative (if mean is negative)
	 * or positive (if mean is positive) peaks are choosen. 
	 * The first and last sample of the given data are not considered for peak calculation.
	 *  
	 * @param w sample data window
	 * @return List with indexes for the input array. The samples at these positions are 
	 * considered as peaks
	 */
	private List<Integer> calcPeaks(double[] w){
		double[] window = w.clone();
		
		int minPeakDistance = 10;
		int minPeakCount = 3;
		int threshold = 95;
		int minThreshold = 70;
		double thresholdValue = 0.0;
		double maxPeak = Double.MIN_VALUE;;		
		ArrayList<Integer> peaks = new ArrayList<Integer>();
				
		double mean = this.calcMean(window);
		if(mean<0){//turn around values, so that negatives peaks are counted
			for(int i=0;i<window.length;i++){
				window[i] *= -1;
			}
		}
		
		window = AverageFilter.filter(window, 3); //remove outlier
		
		for(int i=1;i<window.length-1;i++){ //determine the max. peak; ignore the first and the last sample to get a better peak quality
			if(window[i]>maxPeak){
				maxPeak = window[i];
			}
		}
		maxPeak = Math.min(maxPeak, 2.0); //avoid overlarge max-peaks caused by truncated-peaks prediction
		thresholdValue = maxPeak * threshold / 100.0;
		
		while(peaks.size()<minPeakCount && threshold>=minThreshold){ //try to get at least "minPeakCount" Peaks
			peaks.clear();
			for(int i=1;i<window.length-1;i++){//ignore the first and the last sample to get a better peak quality
				if(window[i]>thresholdValue){
					peaks.add(Integer.valueOf(i));					
				}				
			}
			
			//decrease threshold values
			threshold -= 2;
			thresholdValue = maxPeak * threshold / 100.0;			
			
			//remove nearby peaks (according to minPeakDistance)
			Integer[] peaksCopy = new Integer[peaks.size()];
			peaks.toArray(peaksCopy);
			for(int i=0;i<peaks.size()-1;i++){
				for(int j=i+1;j<peaks.size();j++){
					if((peaks.get(j)-peaks.get(i))<=minPeakDistance){
						if(window[peaks.get(i)] < window[peaks.get(j)]){
							peaksCopy[i] = -1;						
						}
						else{
							peaksCopy[j] = -1;	
						}
					}
				}
			}
			peaks.clear();
			for(int i=0;i<peaksCopy.length;i++){
				if(peaksCopy[i]>=0){
					peaks.add(peaksCopy[i]);
				}
			}
		}
		
		return peaks;
	}
	
	/**
	 * calc the mean of the given data
	 * @param window data to calc the mean for
	 * @return mean of the given data
	 */
	private double calcMean(double[] window){
		double mean = 0.0;		
		for(int i=0;i<window.length;i++){
			mean += window[i];
		}
		mean /= window.length;
		return mean;
	}
	
	/**
	 * calcs the variance of the given data
	 * @param window data to calc the variance for
	 * @param mean the mean value of the given data
	 * @return variance of the given data
	 */
	private double calcVariance(double[] window, double mean){
		double var = 0.0;
		for(int i=0;i<window.length;i++){
			var += Math.pow(window[i]-mean,2);
		}
		var /= window.length;
		return var;
	}

	/**
	 * The model order used for calculating the AR coefficients
	 * @return the arModelOrder
	 */
	public int getArModelOrder() {
		return arModelOrder;
	}

	/**
	 * Sets the model order used for calculating the AR coefficients
	 * @param arModelOrder the arModelOrder to set
	 */
	public void setArModelOrder(int arModelOrder) {
		this.arModelOrder = arModelOrder;
	}

	/** 
	 * returns whether the auto regressive coefficients are used as part of the FV
	 * @return true/false
	 */
	public boolean isUseAR() {
		return useAR;
	}

	/**Enables/Disables the use of the autoregressive coefficients as part of the FV
	 * @param useAR the useAR to set
	 */
	public void setUseAR(boolean useAR) {
		this.useAR = useAR;
	}

	/**returns whether the signal magnitude area is used as part of the FV
	 * @return the useSMA
	 */
	public boolean isUseSMA() {
		return useSMA;
	}

	/**Enables/Disables the use of the signal magnitude area as part of the FV
	 * @param useSMA the useSMA to set
	 */
	public void setUseSMA(boolean useSMA) {
		this.useSMA = useSMA;
	}

	/**returns whether the tilt angle is used as part of the FV
	 * @return the useTA
	 */
	public boolean isUseTA() {
		return useTA;
	}

	/**Enables/Disables the use of the tilt angle as part of the FV
	 * @param useTA the useTA to set
	 */
	public void setUseTA(boolean useTA) {
		this.useTA = useTA;
	}

	/**returns whether the average peak amplitude is used as part of the FV
	 * @return the useAPA
	 */
	public boolean isUseAPA() {
		return useAPA;
	}

	/**Enables/Disables the use of the average peak amplitude as part of the FV
	 * @param useAPA the useAPA to set
	 */
	public void setUseAPA(boolean useAPA) {
		this.useAPA = useAPA;
	}

	/**returns whether the surrounding segmentation rate is used as part of the FV
	 * @return the useSSR
	 */
	public boolean isUseSSR() {
		return useSSR;
	}

	/**Enables/Disables the use of the surrounding segmentation rate as part of the FV
	 * @param useSSR the useSSR to set
	 */
	public void setUseSSR(boolean useSSR) {
		this.useSSR = useSSR;
	}

	/**returns whether mean and variance are used as part of the FV
	 * @return the useMV
	 */
	public boolean isUseMV() {
		return useMV;
	}

	/**Enables/Disables the use of mean and variance as part of the FV
	 * @param useMV the useMV to set
	 */
	public void setUseMV(boolean useMV) {
		this.useMV = useMV;
	}

}
