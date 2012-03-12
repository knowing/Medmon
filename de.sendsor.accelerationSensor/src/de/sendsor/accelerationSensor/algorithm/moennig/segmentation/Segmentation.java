package de.sendsor.accelerationSensor.algorithm.moennig.segmentation;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import scala.Option;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.WekaException;
import akka.actor.ActorRef;
import de.sendsor.accelerationSensor.algorithm.moennig.segmentation.SegmentationFactory;
import de.lmu.ifi.dbs.knowing.core.japi.AbstractProcessor;
import de.lmu.ifi.dbs.knowing.core.japi.JProcessor;
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil;
import de.lmu.ifi.dbs.knowing.core.events.Results;


public class Segmentation extends AbstractProcessor {
	
	private static final int REL_ATT_INDEX = 2;
	
	private static final String SSR_ATTRIBUTE_NAME = "SSR";
	
	private static final int SSR_WINDOW = 1500; //amout of timestamps to calc the surrounding segmentation rate (25Hz*60=1500 -> 1 Minute)
	
	//TODO: introduce a new optional parameter for this value
	private static final int TIME_BETWEEN_SAMPLES = 40; //40ms according to 25Hz sample rate
	
	private double minCorrelation = 0.75;
	private int patternSize = 25;	 
	private int minSegmentLength = 100;
	private int shiftSampleLength = 100;
	private int minAttributesWithSegments = 1;
	
	private int sourceAttIndex = -1;
		 
	private int dimensions;
	private int numOuputAttributes;
	private double[][] patterns;
	private int[] shifts;
	private double[] shiftsCorrelation;
	
	public Segmentation(JProcessor wrapper) {
		super(wrapper);
	}	

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
		
		attributes.add(new Attribute(SSR_ATTRIBUTE_NAME)); //attribute for the surroundig segmentation rate
		
		if(inputFormat.attribute(ResultsUtil.ATTRIBUTE_SOURCE())!=null 
				&& !inputFormat.attribute(ResultsUtil.ATTRIBUTE_SOURCE()).equals(inputFormat.classAttribute())){
			attributes.add(new Attribute(ResultsUtil.ATTRIBUTE_SOURCE(),Collections.list(inputFormat.attribute(ResultsUtil.ATTRIBUTE_SOURCE()).enumerateValues())));
			sourceAttIndex = inputFormat.attribute(ResultsUtil.ATTRIBUTE_SOURCE()).index();
		}
		
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
	     result.enable(Capability.DATE_ATTRIBUTES); // date attribute is required as key, but is not processed
	     result.enable(Capability.NUMERIC_ATTRIBUTES); // only numeric attributes are used for processing
	     result.enable(Capability.STRING_CLASS);
	     result.enable(Capability.NOMINAL_CLASS);
	     result.enable(Capability.NO_CLASS);
	     
	     return result;
	}
	*/
	
	
	@Override
	public void process(Instances input, String port, Instances query) {
	    guessAndSetClassLabel(input);
	    
	    Instances segments = determineOutputFormat(input);
		Instances nonSegments = determineOutputFormat(input);
	    
	    Map<String, Instances> splittedInput = ResultsUtil.splitInstanceBySourceJava(input,false);
		
	    for(Instances inst : splittedInput.values()){
	    	//Important: Use the reference to the segments header structure here - avoid cloning the header structure!!!
	    	Instances s = new Instances(segments,0);
	    	//Important: Use the reference to the nonSegments header structure here - avoid cloning the header structure!!!
	    	Instances n = new Instances(nonSegments,0);	    			
	    	
			this.calcSegmentation(inst, s, n);			
			this.calcSurroundingSegmentationRates(inst, s, n);			
							
			try{
				segments = ResultsUtil.appendInstances(segments,s);
				nonSegments = ResultsUtil.appendInstances(nonSegments,n);
			}
			catch(WekaException we){
				we.printStackTrace();
			}		
	    }		    
	    
	    //Create None Object 
	    Option<String> noneString = scala.Option.apply(null);
	    Option<Instances> noneInstances = scala.Option.apply(null);
		sendEvent(new Results(segments, noneString, noneInstances), SegmentationFactory.SEGMENTS());
		sendEvent(new Results(nonSegments, noneString, noneInstances), SegmentationFactory.NONSEGMENTS());
	}
	
	private void calcSegmentation(Instances inst, Instances segments, Instances nonSegments){
       
		boolean isSegment = false;
		
		patterns = new double[dimensions][];
		shifts = new int[dimensions];
        shiftsCorrelation = new double[dimensions];
		double[][] values = new double[dimensions][inst.numInstances()];       

		
		LinkedList<Instance> segment = new LinkedList<Instance>();
		LinkedList<Instance> nonSegment = new LinkedList<Instance>();
		
		int timeAttIndex = -1;
		for(int i=0; i < inst.numAttributes(); i++){
			if(inst.attribute(i).isDate()){
				timeAttIndex = i;
				break;
			}			
		}
       	       
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
                	   //first iteration
                	   if(nonSegment.isEmpty()){
                		   nonSegment.addAll(segment);
                	   }
                	   else{
                		   Timestamp t1 = new Timestamp((long)nonSegment.getFirst().value(timeAttIndex));
                		   Timestamp t2 = new Timestamp((long)segment.getLast().value(timeAttIndex));
                		   //cobine successive nonSegments
                		   if(t2.getTime()-t1.getTime()==TIME_BETWEEN_SAMPLES){                			   
                			   nonSegment.addAll(segment);
                		   }
                		   //add nonSegment to output
                		   else{
                			   nonSegments.add(this.buildOutputInstance(nonSegment,nonSegments));
                			   nonSegment.clear();
                			   nonSegment.addAll(segment);
                		   }
                	   }                	                   	   
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
           
           if(nonSegment.size() >0){
        	   nonSegments.add(this.buildOutputInstance(nonSegment,nonSegments));
           }
           segment.clear();
       }				
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
    
    private Instance buildOutputInstance(LinkedList<Instance> segment, Instances output){
    	Instance result = new DenseInstance(numOuputAttributes);
    	result.setDataset(output);
    	Instance first = segment.getFirst();
    	Instance last = segment.getLast();
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
    	
    	result.setValue(REL_ATT_INDEX, result.attribute(REL_ATT_INDEX).addRelation(segmentData));
    	
    	if(sourceAttIndex>0){
    		result.setValue(output.attribute(ResultsUtil.ATTRIBUTE_SOURCE()), first.stringValue(sourceAttIndex));
    	}
    	
    	if(first.classIndex()>=0){
    		result.setValue(result.classIndex(), first.stringValue(first.classIndex()));
    	}
    	
    	return result;
    }
    
	public void calcSurroundingSegmentationRates(Instances inst, Instances segments, Instances nonSegments){		
			
		int timeAttIndex = -1;
		for(int i=0; i < inst.numAttributes(); i++){
			if(inst.attribute(i).isDate()){
				timeAttIndex = i;
				break;
			}			
		}
		
		//set segmentation borders
		Vector<Timestamp> segmentStarts = new Vector<Timestamp>();
		Vector<Timestamp> segmentEnds = new Vector<Timestamp>();
		for(Instance in : segments){
			segmentStarts.add(new Timestamp((long)(in.relationalValue(REL_ATT_INDEX).firstInstance().value(timeAttIndex))));
			segmentEnds.add(new Timestamp((long)(in.relationalValue(REL_ATT_INDEX).lastInstance().value(timeAttIndex))));
		}
		Collections.sort(segmentStarts);
		Collections.sort(segmentEnds);						
		
		//calc segmentation rates
		HashMap<Timestamp, Double> segmentationRates = new HashMap<Timestamp, Double>();
		int segIndex = 0;
		for(int i=0;i<inst.numInstances(); i += SSR_WINDOW ){
			int segCount = 0;
			int nonSegCount = 0;
			for(int j=i;j<Math.min(inst.numInstances(), i+SSR_WINDOW);j++){
				Timestamp t = new Timestamp((long)inst.get(j).value(timeAttIndex));
				if((t.equals(segmentStarts.get(segIndex)) || t.after(segmentStarts.get(segIndex))) && t.before(segmentEnds.get(segIndex))){
					segCount++;
				}
				else if(t.equals(segmentEnds.get(segIndex))){
					segCount++;
					segIndex = Math.min(segIndex+1, segmentStarts.size()-1);
				}
				else{
					nonSegCount++;
				}
			}
			Double rate = Double.valueOf((double)(segCount)/(double)(segCount+nonSegCount));
			segmentationRates.put(new Timestamp((long)inst.get(i).value(timeAttIndex)), rate);				
		}
		
		setSSRValues(segmentationRates, segments, timeAttIndex);
		setSSRValues(segmentationRates, nonSegments, timeAttIndex);
	}
	
	private void setSSRValues(Map<Timestamp,Double> segmentationRates, Instances inst, int timeAttIndex){
		List<Timestamp> ts = new ArrayList<Timestamp>(segmentationRates.keySet());
		Collections.sort(ts);
		
		for(Instance in : inst){
			Timestamp start = new Timestamp((long)in.relationalValue(REL_ATT_INDEX).firstInstance().value(timeAttIndex));
			Timestamp end = new Timestamp((long)in.relationalValue(REL_ATT_INDEX).lastInstance().value(timeAttIndex));
		
			int i=0;
			while(i < ts.size()-1 && start.before(ts.get(i))){
				i++;
			}
			double rate = segmentationRates.get(ts.get(i));
			int buckets = 1;
			i++;
			while(i < ts.size() && end.after(ts.get(i))){
				rate += segmentationRates.get(ts.get(i));
				buckets++;
				i++;
			}
			in.setValue(inst.attribute(SSR_ATTRIBUTE_NAME), (rate/buckets));
		}
	}
	
	@Override
	public Instances query(Instances query, ActorRef ref) {
		throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
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
