package de.sendsor.accelerationSensor.algorithm.moennig.segmentation;

import java.util.ArrayList;
import java.util.Vector;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Instance;
import weka.core.Capabilities.Capability;
import weka.filters.SimpleBatchFilter;

public class Segmentation extends SimpleBatchFilter {
	
	private static final long serialVersionUID = 8254607844622030623L;
	
	private double minCorrelation = 0.75;
	private int patternSize = 25;	 
	private int minLengthSegment = 100;
	private int lengthShiftSample = 100;
	private int minDimensionsWithSegments = 1;
		 
	private int dimensions;
	private int numOuputAttributes;
	private double[][] patterns;
	private int[] shifts;
	private double[] shiftsCorrelation;
	
	@Override
	public String globalInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Instances determineOutputFormat(Instances inputFormat) throws Exception {

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
		attributes.add(new Attribute("segment", inputFormat.stringFreeStructure())); //relational attribute for the input samples belonging to the segment
		
		if(inputFormat.classIndex()>=0){
			attributes.add(inputFormat.attribute(inputFormat.classIndex())); //add class if inputFormat contains a class value
		}
		
		numOuputAttributes = attributes.size();
		Instances result = new Instances(inputFormat.relationName(),attributes, 0);
		return result;
	}
	
	@Override
	public Capabilities getCapabilities() {
	     Capabilities result = super.getCapabilities();	     
	     result.enable(Capability.DATE_ATTRIBUTES); // date attribute is accepted, but is not processed
	     result.enable(Capability.NUMERIC_ATTRIBUTES); // only numeric attributes are used for processing
	     result.enable(Capability.STRING_CLASS);
	     result.enable(Capability.NO_CLASS);
	     
	     return result;
	}

	@Override
	protected Instances process(Instances input) throws Exception {
		Instances out = determineOutputFormat(input);
		out = this.calcSegmentation(input, out);
		return out;
	}
	
	private Instances calcSegmentation(Instances inst, Instances out){
       
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
           for(int i = 0; i <= values[0].length-lengthShiftSample; i=i+shifts[bestShift]){
               if(!isSegment){
                   getPatterns(i,values);
                   calcShifts(i,values);
                   bestShift = bestShift();
               }
        	   
               int correlations = calcCorrelations(i, values);

               if(correlations >= minDimensionsWithSegments){            	   
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
                   if(segment.size() > minLengthSegment){
                	   out.add(this.buildOutputInstance(segment,out));
                	   segment.clear();
                   }
                   else{
                	   //TODO Non Segments
                   }
                   
                   i=i-shifts[bestShift];
                   isSegment=false;
               }               
           }

           if(segment.size() > minLengthSegment){
        	   out.add(this.buildOutputInstance(segment,out));
        	   segment.clear();
          }
          else{
        	  //TODO Non Segments
          }
       }
		
		return out;
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
            double[] sample = new double[lengthShiftSample];
            System.arraycopy(values[i], startposition, sample, 0, lengthShiftSample);
            double correlation = Double.NEGATIVE_INFINITY;
            int shift = 0;
            AutoCorrelation autocorrelation = new AutoCorrelation();
            for(int j = 1; j<= lengthShiftSample-patternSize; j++){
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
    	    	
    	result.setValue(2, result.attribute(2).addRelation(segmentData));
    	
    	if(first.classIndex()>=0){
    		result.setClassValue(first.classValue());
    	}
    	
    	return result;
    }

}
