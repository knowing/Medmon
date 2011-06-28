package de.sendsor.accelerationSensor.algorithm.moennig.fv;

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
	
	private boolean useAR = true; //use auto regressive coefficients

	private int relAttIndex; //index of the relational attribute
	private int dimensions; //amount of numeric attributes in the relational attribute (used for building the fv)
	private int numFVattributes; //amount of new fv attributes in the output format
	
	@Override
	protected Instances determineOutputFormat(Instances inputFormat) throws Exception {
		relAttIndex = -1;				
		for(int i=0;i<inputFormat.numAttributes() && relAttIndex<0;i++){
			if(inputFormat.attribute(i).isRelationValued()){
				relAttIndex = i;				
			}
		}
		
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
	    			 result.insertAttributeAt(new Attribute("AR_"+i+"_"+j), result.numAttributes());
	    			 numFVattributes++;
	    		 }
	    	 }	    	 
	    }
	    	 
	    return result;
	}

	@Override
	public String globalInfo() {
		// TODO Auto-generated method stub
		return null;
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
		//determine the index of the relational attribute

		
		
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
		
		//calculate AR coeffients for each dimension
		if(useAR){			
			for(int j=0;j<numData.length;j++){
				double[] ar = AutoRegression.calculateARCoefficients(numData[j], arModelOrder, true);													
				for(double c : ar){
					fvValues[fvIndex] = c;
					fvIndex++;
				}								
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

}
