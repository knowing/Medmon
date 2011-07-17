package de.sendsor.accelerationSensor.algorithm.moennig.segmentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.Vector;

import akka.actor.ActorRef;

import de.lmu.ifi.dbs.knowing.core.events.Results;
import de.lmu.ifi.dbs.knowing.core.japi.AbstractProcessor;
import de.lmu.ifi.dbs.knowing.core.japi.JProcessor;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Instance;
import weka.core.Capabilities.Capability;
import weka.filters.SimpleBatchFilter;

public class Segmentation extends AbstractProcessor {
	
	private static final long serialVersionUID = 8254607844622030623L;
	
	private static final int REL_ATT_INDEX = 2;
	
	private double minCorrelation = 0.75;
	private int patternSize = 25;	 
	private int minSegmentLength = 100;
	private int shiftSampleLength = 100;
	private int minAttributesWithSegments = 1;
		 
	private int dimensions;
	private int numOuputAttributes;
	private double[][] patterns;
	private int[] shifts;
	private double[] shiftsCorrelation;
	
	public Segmentation(JProcessor wrapper) {
		super(wrapper);
	}	

	@Override
	public Instances query(Instance query, ActorRef ref) {
		return null;
	}

	@Override
	public void result(Instances result, Instance query) {}

	@Override
	public void configure(Properties properties) {}

	@Override
	public void messageException(Object message) {}
	
		
	public String globalInfo() {
		return "This batch filter finds periodical areas in the input samples and groups them into segments. " +
				"Vice versa non periodical areas and too short periodical areas (see min \"minSegmentLength\") are grouped into non-segment.";
	}
	
	protected Instances determineOutputFormat(Instances inputFormat) {

		//determine the dimensionality of the inputFormat	
		dimensions = 0;
		for(int j=0; j < inputFormat.numAttributes(); j++){
		   if(inputFormat.classIndex()!=j && inputFormat.attribute(j).type() == Attribute.NUMERIC){
			   dimensions++;
		   }
		}
		
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(new Attribute("start","yyyy-MM-dd\" \"HH:mm:ss.SS")); //segment start time
		attributes.add(new Attribute("end","yyyy-MM-dd\" \"HH:mm:ss.SS")); //segment end time
		
		Instances relFormat = inputFormat.stringFreeStructure();
		relFormat.setClassIndex(-1); //otherwise the class attribute won't be present in the relational attribute
		attributes.add(new Attribute("segment", relFormat)); //relational attribute for the input samples belonging to the segment
		
		int classIndex = -1;
		if(inputFormat.classIndex()>=0){
			//add class if inputFormat contains a class value
			attributes.add(new Attribute(inputFormat.classAttribute().name(),Collections.list(inputFormat.classAttribute().enumerateValues())));
			classIndex = attributes.size()-1;
		}
		
		numOuputAttributes = attributes.size();
		Instances result = new Instances(inputFormat.relationName(),attributes, 0);
		result.setClassIndex(classIndex);
		return result;
	}
	
	/*
	@Override
	public Capabilities getCapabilities() {
	     Capabilities result = super.getCapabilities();	     
	     result.enable(Capability.DATE_ATTRIBUTES); // date attribute is accepted, but is not processed
	     result.enable(Capability.NUMERIC_ATTRIBUTES); // only numeric attributes are used for processing
	     result.enable(Capability.STRING_CLASS);
	     result.enable(Capability.NOMINAL_CLASS);
	     result.enable(Capability.NO_CLASS);
	     
	     return result;
	}
	*/
	
	@Override
	public void build (Instances input) {
			    
	    guessAndSetClassLabel(input);
		
		Instances segments = determineOutputFormat(input);
		Instances nonSegments = determineOutputFormat(input);
		
		this.calcSegmentation(input, segments, nonSegments);
		
		sendEvent(new Results(segments), SegmentationFactory.SEGMENTS());
		sendEvent(new Results(nonSegments), SegmentationFactory.NONSEGMENTS());
	}
	
	private Instances calcSegmentation(Instances inst, Instances segments, Instances nonSegments){
       
		boolean isSegment = false;
		
		patterns = new double[dimensions][];
		shifts = new int[dimensions];
        shiftsCorrelation = new double[dimensions];
		double[][] values = new double[dimensions][inst.numInstances()];       

		Vector<Instance> segment = new Vector<Instance>();		
       	       
		for(int i = 0; i < inst.numInstances(); i++){
		   int d = 0;
		   for(int j=0; j < inst.numAttributes(); j++){
			   if(inst.classIndex()!=j && inst.attribute(j).type() == Attribute.NUMERIC){
				   values[d][i] = inst.get(i).value(j);
				   d++;
			   }
		   }
		}
       
		getPatterns(0,values);
		calcShifts(0, values);
		int bestShift = bestShift();
		
		if(shifts[bestShift]>0){
           for(int i = 0; i <= values[0].length-shiftSampleLength; i=i+shifts[bestShift]){
               if(!isSegment){
                   getPatterns(i,values);
                   calcShifts(i,values);
                   bestShift = bestShift();
               }
        	   
               int correlations = calcCorrelations(i, values);

               if(correlations >= minAttributesWithSegments){            	   
            	   for(int j=i;j<i+shifts[bestShift];j++){
            		   segment.add(inst.get(j));
            	   }                
            	   if(!isSegment){
                       getPatterns(i, values);
                       calcShifts(i, values);
            	   }
            	   isSegment = true;
               }
               else{
                   if(segment.size() > minSegmentLength){
                	   segments.add(this.buildOutputInstance(segment,segments));                	   
                   }
                   else{
                	   nonSegments.add(this.buildOutputInstance(segment,nonSegments));                	   
                   }
                   
                   segment.clear();
                   i=i-shifts[bestShift];
                   isSegment=false;
               }               
           }

           if(segment.size() > minSegmentLength){
        	   segments.add(this.buildOutputInstance(segment,segments));        	   
           }
           else{
        	  nonSegments.add(this.buildOutputInstance(segment,nonSegments));       	   
           }
           segment.clear();
       }
		
		return segments;
    }
	 
    private void getPatterns(int startposition, double[][] values){    	
        for(int i = 0 ; i < dimensions; i++){            
            double[] pattern = new double[patternSize];
            System.arraycopy(values[i],startposition,pattern,0,patternSize);
            patterns[i] = pattern;
        }        
    }
    
    private void calcShifts(int startposition, double[][] values){
        for(int i = 0; i < dimensions; i++){            
            double[] sample = new double[shiftSampleLength];
            System.arraycopy(values[i], startposition, sample, 0, shiftSampleLength);
            double correlation = Double.NEGATIVE_INFINITY;
            int shift = 0;
            AutoCorrelation autocorrelation = new AutoCorrelation();
            for(int j = 1; j<= shiftSampleLength-patternSize; j++){
                double[] testArray = new double[patternSize];
                System.arraycopy(sample, j, testArray, 0, patternSize);
                double tempcorr = autocorrelation.calcAutocorrelation(testArray, patterns[i]);
                tempcorr = Math.round(tempcorr*1000000.)/1000000.;
                if(tempcorr > correlation){
                    correlation = tempcorr;
                    shift = j;
                }
            }
            shifts[i] = shift;
            shiftsCorrelation[i] = correlation;
        }        
    }
    
    private int bestShift(){
        int result = -1;
        double bestcorrelation = Double.NEGATIVE_INFINITY;
        for(int i = 0; i < dimensions; i++){
            if(shiftsCorrelation[i]> bestcorrelation){
                bestcorrelation = shiftsCorrelation[i];
                result = i;
            }
        }
        return result;
    }
    
    private int calcCorrelations(int startposition, double[][] values){
        AutoCorrelation autocorr = new AutoCorrelation();
        int count = 0;
        for(int i = 0; i < dimensions; i++){
		  double[] array = new double[patternSize];
		  System.arraycopy(values[i], startposition, array, 0, patternSize);
		  double corr = autocorr.calcAutocorrelation(patterns[i], array);
		  if(corr >= minCorrelation){
		      count++;
		  }
        }        
        return count;
    }
    
    private Instance buildOutputInstance(Vector<Instance> segment, Instances output){
    	Instance result = new DenseInstance(numOuputAttributes);
    	result.setDataset(output);
    	Instance first = segment.firstElement();
    	Instance last = segment.lastElement();
    	for(int i=0;i<first.numAttributes();i++){
    		if(first.attribute(i).type()==Attribute.DATE){
    			result.setValue(0, first.value(i));		
    		}
    	}
    	for(int i=0;i<last.numAttributes();i++){
    		if(last.attribute(i).type()==Attribute.DATE){
    			result.setValue(1, last.value(i));		
    		}
    	}    	
    	
    	Instances segmentData = new Instances(first.dataset(),0);
    	for(Instance s : segment){
    		segmentData.add(s);
    	}
    	segmentData.setClassIndex(result.attribute(REL_ATT_INDEX).relation().classIndex()); //make header equal!    	
    	
    	result.setValue(2, result.attribute(REL_ATT_INDEX).addRelation(segmentData));
    	
    	if(first.classIndex()>=0){
    		result.setValue(result.classIndex(), first.stringValue(first.classIndex()));
    	}
    	
    	return result;
    }
    
	public double getMinCorrelation() {
		return minCorrelation;
	}

	public void setMinCorrelation(double minCorrelation) {
		this.minCorrelation = minCorrelation;
	}

	/**
	 * @return the patternSize
	 */
	public int getPatternSize() {
		return patternSize;
	}

	/**
	 * @param patternSize the patternSize to set
	 */
	public void setPatternSize(int patternSize) {
		this.patternSize = patternSize;
	}

	/**
	 * @return the minSegmentLength
	 */
	public int getMinSegmentLength() {
		return minSegmentLength;
	}

	/**
	 * @param minSegmentLength the minSegmentLength to set
	 */
	public void setMinSegmentLength(int minSegmentLength) {
		this.minSegmentLength = minSegmentLength;
	}

	/**
	 * @return the shiftSampleLength
	 */
	public int getShiftSampleLength() {
		return shiftSampleLength;
	}

	/**
	 * @param shiftSampleLength the shiftSampleLength to set
	 */
	public void setShiftSampleLength(int shiftSampleLength) {
		this.shiftSampleLength = shiftSampleLength;
	}

	/**
	 * @return the minAttributesWithSegments
	 */
	public int getMinAttributesWithSegments() {
		return minAttributesWithSegments;
	}

	/**
	 * @param minAttributesWithSegments the minAttributesWithSegments to set
	 */
	public void setMinAttributesWithSegments(int minAttributesWithSegments) {
		this.minAttributesWithSegments = minAttributesWithSegments;
	}




}
