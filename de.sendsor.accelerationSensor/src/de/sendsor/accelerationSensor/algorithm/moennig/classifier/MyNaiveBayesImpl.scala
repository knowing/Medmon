package de.sendsor.accelerationSensor.algorithm.moennig.classifier
import weka.core.Instances
import java.util.ArrayList
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instance

class MyNaiveBayesImpl extends weka.classifiers.bayes.NaiveBayes{   
  
  var inputFormat:Instances = null;
  
  def deterimeInputFormat(data:Instances){
    
    var numNumericAtts:Int = 0;
    //detemine the attributes for 	  
	for(i <- 0 until data.numAttributes){
	    if(data.attribute(i).name().startsWith("FV_") && data.attribute(i).`type` == Attribute.NUMERIC && data.classIndex!=i){
	      numNumericAtts += 1
	    }
	}
	
	var atts:ArrayList[Attribute] = new ArrayList[Attribute];
	for(i <- 0 until numNumericAtts){
		atts.add(new Attribute("FV_"+i))
	}
	atts.add(data.classAttribute);
	
	inputFormat = new Instances("inputFormat", atts, 0);
	inputFormat.setClassIndex(inputFormat.numAttributes-1);
  }

  override def buildClassifier(data:Instances){
	  deterimeInputFormat(data);
	  
	  val train:Instances = new Instances(inputFormat, 0);
	  
	  for(i <- 0 until data.numInstances){
	    var inst:DenseInstance = new DenseInstance(inputFormat.numAttributes());
	    var count:Int = 0;
	    for(j <- 0 until data.numAttributes){
	      if(data.attribute(j).name().startsWith("FV_") && data.attribute(j).`type` == Attribute.NUMERIC && data.classIndex!=j){
	        inst.setValue(count,data.get(i).value(j));
	        count += 1;
	      }
	    }
	    inst.setValue(inst.numAttributes-1,data.get(i).value(data.get(i).classIndex));
	    train.add(inst);
	  }
	  
	  super.buildClassifier(train);
	  
  }
  
  override def classifyInstance(instance:Instance):Double = {
    
	  	var test:DenseInstance = new DenseInstance(inputFormat.numAttributes());
	  	var count:Int = 0;
	    for(j <- 0 until instance.numAttributes){
	      if(instance.attribute(j).name().startsWith("FV_") && instance.attribute(j).`type` == Attribute.NUMERIC && instance.classIndex!=j){
	        test.setValue(count,instance.value(j));
	        count += 1;
	      }
	    }
	    
	    return super.classifyInstance(test); 
  }
  
  override def distributionForInstance(instance:Instance):Array[Double] = {
   	var test:DenseInstance = new DenseInstance(inputFormat.numAttributes());
	  	var count:Int = 0;
	    for(j <- 0 until instance.numAttributes){
	      if(instance.attribute(j).name().startsWith("FV_") && instance.attribute(j).`type` == Attribute.NUMERIC && instance.classIndex!=j){
	        test.setValue(count,instance.value(j));
	        count += 1;
	      }
	    }
	    
	    return super.distributionForInstance(test);  
  }
  
}