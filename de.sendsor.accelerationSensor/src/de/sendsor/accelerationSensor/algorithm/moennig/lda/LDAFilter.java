package de.sendsor.accelerationSensor.algorithm.moennig.lda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import de.lmu.ifi.dbs.knowing.core.japi.ILoggableProcessor;
import de.lmu.ifi.dbs.knowing.core.processing.TProcessor;
import de.lmu.ifi.dbs.knowing.core.events.*;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.SimpleBatchFilter;

import de.lmu.ifi.dbs.knowing.core.japi.LoggableProcessor;

public class LDAFilter extends SimpleBatchFilter implements ILoggableProcessor {
	
	private final static long serialVersionUID = 1L;
	
	private final static double DIMENSION_REDUCTION_PER = 0.95;		
	private final static double SINGULARITY_DETECTION_THRESHOLD = 1.0E-250;	
	
	private boolean dimReduction = false;
	private int outDimensions;
	private String attributeNamePrefix; //use only attributes with the given name prefix for the LDA
	
	private int inDimensions;	
	private Matrix eigenVectorMatrix;
	private double[] eigenValues;
	
	private Instances output;

	private LoggableProcessor log;
		
	
	@Override
	public String globalInfo() {		
		return "This batch filter perfoms a linear discriminant analysis (LDA) on all numeric attributes of the instances.\n\n" +
				"For furhter details on the LDA have a look at the Article 'Eigenfaces vs. Fisherfaces: recognition using class " +
				"specific linear projection' by Belhumeur et al. (1997).";
	}
	
	@Override
	protected Instances determineOutputFormat(Instances inputFormat) throws Exception {
		if(output==null){
			//determine the amount of numeric input dimensions
			determineInputDimensions(inputFormat);
			
			Instances result = new Instances(inputFormat, 0);
			
			if(dimReduction && outDimensions > 0 ){			
				
				if(inDimensions > outDimensions){
					int toDelete = inDimensions - outDimensions;
					for(int i=inputFormat.numAttributes()-1; toDelete>0 && i>=0; i--){
						if(i!=inputFormat.classIndex() && inputFormat.attribute(i).type() == Attribute.NUMERIC){
							if(attributeNamePrefix == null || inputFormat.attribute(i).name().startsWith(attributeNamePrefix)){
								result.deleteAttributeAt(i);
								toDelete--;
							}
						}
					}
				}
				else{
					throw new IllegalArgumentException("output dimensions have to be lower than input dimensions ("+outDimensions+" !< "+inDimensions+")");
				}
			}
			
			//rename FV attributes
			int fvc = 1;
			for(int i=0;i<result.numAttributes();i++){
				if(attributeNamePrefix != null && result.attribute(i).name().startsWith(attributeNamePrefix)){				
					result.renameAttribute(i, attributeNamePrefix+fvc);
					fvc++;
				}
			}
			output = result;
		}
				
		return output;
	}	
	
	private void determineInputDimensions(Instances inputFormat){
		inDimensions = 0;
		for(int i=0;i<inputFormat.numAttributes();i++){
			if(i!=inputFormat.classIndex() && inputFormat.attribute(i).type() == Attribute.NUMERIC){
				if(attributeNamePrefix == null || inputFormat.attribute(i).name().startsWith(attributeNamePrefix)){
					inDimensions++;
				}
			}
		}
	}
	
	@Override
	public Capabilities getCapabilities() {
	     Capabilities result = super.getCapabilities();	     
	     result.enable(Capability.DATE_ATTRIBUTES); // date attribute is accepted, but will only be passed through
	     result.enable(Capability.RELATIONAL_ATTRIBUTES); // relational attributes are accepted, but will only be passed through
	     result.enable(Capability.NUMERIC_ATTRIBUTES); // only numeric attributes are proccessed
	     result.enable(Capability.NOMINAL_CLASS);  // filter needs a nominal or string class to be set
	     result.enable(Capability.STRING_CLASS);  // filter needs a nominal or string class to be set
	     
	     return result;
	}
	
	@Override
	protected Instances process(Instances input) throws Exception {
		
		Instances output = new Instances(determineOutputFormat(input), 0);
		
		//this.determineProjectionMatrix(input);
						
		if(dimReduction && outDimensions > 0 && inDimensions > outDimensions){			
			return this.transformData(input,output,outDimensions);
		}
		else{
			return this.transformData(input,output);
		}
	}	
	
	/**
	 * perfoms a Eigenvalue decomposition to determine the optimal projection matrix for the given Instances 
	 * @param inst the instances to calculate the LDA for
	 * @throws Exception throws an Exception if one of the covariance matrixes gets singular 
	 */
	public void train(Instances inst) throws Exception{
		log.statusChanged(new Progress("Training", 0, 8));
		determineOutputFormat(inst);
		//determineInputDimensions(inst);
		
		//This cast is save -> capabilities allow only String or Norminal classes
		ArrayList<String> classList = Collections.list((Enumeration<String>)inst.classAttribute().enumerateValues());								
		
		log.statusChanged(new Progress("Create Subsets", 1, 8));
		// divide data into subsets
		ArrayList<ArrayList<ArrayList<Double>>> subset = new ArrayList<ArrayList<ArrayList<Double>>>();
		for (int i = 0; i < classList.size(); i++) {
			String currentClasss = classList.get(i);
			ArrayList<ArrayList<Double>> si = new ArrayList<ArrayList<Double>>();			
			for (int j = 0; j < inst.numInstances(); j++) {		
				Instance in = inst.get(j);
				String cla = inst.classAttribute().value((int)(in.value(inst.classAttribute())));
				if(cla.equals(currentClasss)){
					ArrayList<Double> al = new ArrayList<Double>();
					for(int k=0;k<in.numAttributes();k++){
						if(k!=inst.classIndex() && inst.attribute(k).type() == Attribute.NUMERIC){
							if(attributeNamePrefix == null || inst.attribute(k).name().startsWith(attributeNamePrefix)){
								al.add(in.value(k));
							}
						}
					}
					si.add(al);
				}
			}
			if(si.isEmpty())
				throw new Exception("Empty subset. Would lead to empty covariance matrix!");
			
			subset.add(i, si);
		}
		
		log.statusChanged(new Progress("Calculate groupMean", 2, 8));
		// calculate group mean
		double[][] groupMean = new double[subset.size()][inDimensions];
		for (int i = 0; i < groupMean.length; i++) {
			for (int j = 0; j < groupMean[i].length; j++) {
				groupMean[i][j] = getGroupMean(j, subset.get(i));
			}
		}

		log.statusChanged(new Progress("Calculate totalMean", 3, 8));
		// calculate total mean
		double[] totalMean = new double[inDimensions];
		for (int i = 0; i < totalMean.length; i++) {
			totalMean[i] = getTotalMean(i, inst);
		}
		
		log.statusChanged(new Progress("Calculate covariance matrices", 2, 8));
		// calculate covariance matrices
		double[][][] covariance = new double[subset.size()][inDimensions][inDimensions];
		for (int i = 0; i < subset.size(); i++){
			covariance[i] = getCovarianceMatrix(subset.get(i), groupMean[i]);
		}
		
		log.statusChanged(new Progress("Check matrices", 4, 8));
		//test for matrix singularity
		for(int i=0;i<covariance.length;i++){
			Matrix covM = new Matrix(covariance[i]);
			double det = covM.det();			
			if(Math.abs(det) < SINGULARITY_DETECTION_THRESHOLD || Double.valueOf(det).isNaN()){
				throw new Exception("matrix got singular...");
			}
		}
		
		log.statusChanged(new Progress("Calculate within-class scatter", 5, 8));
		//calculate the within-class scatter matrix
		double[][] sw = new double[inDimensions][inDimensions];
		for (int i = 0; i < covariance.length; i++){
			for(int j=0; j <  covariance[i].length; j++){
				for(int k=0; k < covariance[i][j].length; k++){
					sw[j][k] += covariance[i][j][k];
				}
			}			
		}
		
		log.statusChanged(new Progress("Calculate between-class scatter", 6, 8));
		//calculate the between-class scatter matrix
		double[][] sb = new double[inDimensions][inDimensions];
		for(int i=0; i < subset.size();i++){			
			for(int j=0;j<inDimensions;j++){
				for(int k=0;k<inDimensions;k++){
					//sb[j][k] += subset[i].size() * (groupMean[i][j]-totalMean[j])*(groupMean[i][k]-totalMean[k]);
					sb[j][k] += inDimensions * (groupMean[i][j]-totalMean[j])*(groupMean[i][k]-totalMean[k]);
				}
			}
		}
		
		Matrix sbm = new Matrix(sb);
		Matrix swm = new Matrix(sw);
		
		log.statusChanged(new Progress("EigenvalueDecomposition", 7, 8));
		Matrix criterion = (swm.inverse()).times(sbm);
		EigenvalueDecomposition evd = new EigenvalueDecomposition(criterion);
		eigenVectorMatrix = evd.getV();		
		eigenValues = evd.getRealEigenvalues();
		
		log.statusChanged(new Progress("Sort eigenvalues", 8, 8));
		this.sortEigenvalues();
	}
	
	
	/**
	 * sort the Eigenvalues in descendig order and rearrange the Eigenvektor matrix accordingly 
	 */
	private void sortEigenvalues(){
		double[] evArray = Arrays.copyOf(eigenValues, eigenValues.length);
		double[] evSorted = new double[evArray.length];
		
		HashMap<Double, Vector<Integer>> evMap = new HashMap<Double,Vector<Integer>>();
		for(int i=0;i<evArray.length;i++){
			Vector<Integer> v = evMap.get(evArray[i]);
			if(v==null){
				v = new Vector<Integer>();
			}
			v.add(i);
			evMap.put(evArray[i], v);			
		}
		ArrayList<Double> evList = new ArrayList<Double>(evMap.keySet());
		Collections.sort(evList,Collections.reverseOrder());
		
		
		double[] normFactor = new double[inDimensions];
		for(int i=0;i<inDimensions;i++){
			double absMax = 0.0;
			for(int j=0;j<inDimensions;j++){
				if(Math.abs(eigenVectorMatrix.get(j, i))>absMax){
					absMax = Math.abs(eigenVectorMatrix.get(j, i));
				}
			}
			normFactor[i] = absMax;
		}		
				
		double[][] temp = eigenVectorMatrix.getArrayCopy();
		int newIndex = 0;
		for(int j=0;j<evList.size();j++){
			for(Integer oldIndex : evMap.get(evList.get(j))){				
				for(int k=0;k<temp.length;k++){
					temp[k][newIndex] = eigenVectorMatrix.get(k, oldIndex)/normFactor[oldIndex];
				}				
				evSorted[newIndex] = evList.get(j).doubleValue();
				newIndex++;
			}
		}		
		eigenVectorMatrix = new Matrix(temp);
		eigenValues = evSorted;			
	}
	
	private Instances transformData(Instances input, Instances output) throws Exception{		
		return transformData(input, output, eigenValues.length);		
	}
	
	//TODO: find a way to set the ouput format after proccessing the input (after determineProjectionMatrix)
	
	/*not used at the moment because the output format is determined before processing and a dynamic	
	 *adaptation of the output format isn't allowed
	 */
	private Instances transformData(Instances input, Instances output, boolean dimensionReduction) throws Exception{
		if(dimensionReduction){
			double sum = 0.0;
			double totalPer = 1.0;
			int discriminants = eigenValues.length;
			for(double ev : eigenValues){
				sum += ev;
			}
			for(int i=eigenValues.length-1;i>=0;i--){
				double per = eigenValues[i]/sum;
				if((totalPer-per)>DIMENSION_REDUCTION_PER){
					totalPer -= per;
					discriminants--;					
				}
			}			
			return transformData(input, output, discriminants);
		}
		else{
			return transformData(input, output, eigenValues.length);
		}
	}
		
	private Instances transformData(Instances input, Instances output, int discriminants) throws Exception{		
		double[][] eva = eigenVectorMatrix.getArray();
		double[][] tempEva = new double[eva.length][discriminants];		
		for(int i=0;i<eva.length;i++){
			tempEva[i] = Arrays.copyOf(eva[i], discriminants);
		}
		double[][] data = new double[input.numInstances()][inDimensions];
		
		for(int i=0;i<input.numInstances();i++){
			Instance in = input.get(i);
			int dj = 0;
			for(int j=0;j<input.numAttributes();j++){				
				if(j!=in.classIndex() && in.attribute(j).type() == Attribute.NUMERIC
					&& (attributeNamePrefix == null || in.attribute(j).name().startsWith(attributeNamePrefix))){
						data[i][dj] = in.value(j);
						dj++;
				}
			}
		}
		
		Matrix evm = new Matrix(tempEva);
		Matrix dataM = new Matrix(data);
		Matrix resultM = (evm.transpose()).times(dataM.transpose());
		
		
		double[][] rm =  resultM.transpose().getArrayCopy();
		
		for(int i=0;i<input.numInstances();i++){
			Instance in = input.get(i);
			Instance r = new DenseInstance(output.numAttributes());
			r.setDataset(output);
			int rj = 0;
			for(int j=0;j<output.numAttributes();j++){
				if(j!=in.classIndex() && in.attribute(j).type() == Attribute.NUMERIC
					&& (attributeNamePrefix == null || in.attribute(j).name().startsWith(attributeNamePrefix))){
					if(rj < discriminants){
						r.setValue(j, rm[i][rj]);
						rj++;				
					}
				}	
				else{
					if(in.attribute(j).isRelationValued()){
						r.setValue(j, output.attribute(j).addRelation(in.relationalValue(j)));				
					}
					else{
						r.setValue(j, in.value(j));
					}
				}								
			}
						
			
			output.add(r);
		}
				
		
		return output;
	}
	
	/**
	 * calculates the mean value for the column at the given index
	 * @param column index of the column to calculate the mean for
	 * @param data feature vectors to calculate the mean for
	 * @return mean value
	 */
	private static double getGroupMean(int column, ArrayList<ArrayList<Double>> data) {
		double[] d = new double[data.size()];
		for (int i = 0; i < data.size(); i++) {
			d[i] = data.get(i).get(column);
		}

		return getMean(d);
	}
	
	/**
	 * calculates the mean value for the attribute at the given index over all instances
	 * @param attIndex index of the attribute to calculate the mean for
	 * @param inst instances to calculate the mean for
	 * @return mean value
	 */
	private static double getTotalMean(int attIndex, Instances inst) {
		double[] d = new double[inst.numInstances()];
		for (int i = 0; i < inst.numInstances(); i++) {
			d[i] = inst.get(i).value(attIndex);
		}

		return getMean(d);
	}
	
	/**
	 * Returns the mean of the given values. On error or empty data returns 0.
	 * 
	 * @param values The values.
	 * @return The mean.
	 */
	private static double getMean(final double[] values) {
		if (values == null || values.length == 0)
			return Double.NaN;

		double mean = 0.0d;

		for (int index = 0; index < values.length; index++){
			mean += values[index];
		}

		return mean / (double) values.length;
	}
	
	/**
	 * calculates the covariance matrix for the given feature vectors
	 * @param fvs the the feature vectors to calculate the covariances for
	 * @param means array with mean values for each feature in the feature vector
	 * @return covariance matrix
	 */
	private static double[][] getCovarianceMatrix(ArrayList<ArrayList<Double>> fvs, double[] means){		
		int dimension = fvs.get(0).size();
		double[][] covariance = new double[dimension][dimension];
		for (int i = 0; i < dimension; i++) {
			for (int j = i; j < dimension; j++) {
				double s = 0.0;
				for (int k = 0; k < fvs.size(); k++) {					
					s += (fvs.get(k).get(j) - means[j]) * (fvs.get(k).get(i) - means[i]);
				}
				s /= fvs.size();
				covariance[i][j] = s;
				covariance[j][i] = s;
			}
		}		
		return covariance;
	}

	
	/**
	 * Enables/disables the dimension reduction. 
	 * Dimension reduction only works if a valid value for the amout of output dimensions is set.
	 * @param b true/false
	 */
	public void setDimensionReduction(boolean b){
		this.dimReduction = b;
	}
	
	/**
	 * returns whether dimension reduction is enabled or disabled
	 * @return true/false
	 */
	public boolean isDimensionReduction(){
		return dimReduction;
	}
	
	/**
	 * returns the amount of attributes to use in the ouput if dimension reduction is enabled
	 * @return the outDimensions
	 */
	public int getOutDimensions() {
		return outDimensions;
	}
	
	/**
	 * Sets the value for the amout of output dimensions
	 * @param outDimensions amout of output dimensions
	 */
	public void setOutDimensions(int outDimensions){
		this.outDimensions = outDimensions;
	}

	/**
	 * Sets an prefix for the attribute name. Only those attributes whose names start with the prefix are considered for the LDA
	 * @param attributeNamePrefix the attributeNamePrefix to set
	 */
	public void setAttributeNamePrefix(String attributeNamePrefix) {
		this.attributeNamePrefix = attributeNamePrefix;
	}

	/**The prefix or null if no prefix has been set
	 * @return the attributeNamePrefix
	 */
	public String getAttributeNamePrefix() {
		return attributeNamePrefix;
	}

	/**
	 * Used for communication with the akka-actor-framework
	 * Especially for logging and status updates.
	 * @param processor - the ActorRef
	 */
	@Override
	public void setProcessor(TProcessor processor) {
		log = new LoggableProcessor(processor);
	}


	
}
