package de.sendsor.accelerationSensor.algorithm.moennig.preprocessing

import TruncatedPeakPrediction._
import TruncatedPeakPredictionFactory._
import weka.core.Instances
import de.lmu.ifi.dbs.knowing.core.processing.TProcessor
import java.util.Properties
import weka.core.Instance
import weka.core.Attribute
import akka.actor.ActorRef
import de.lmu.ifi.dbs.knowing.core.events.Results
import de.lmu.ifi.dbs.knowing.core.factory.TFactory
import akka.actor.ActorRef
import akka.actor.Actor.actorOf
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil

class TruncatedPeakPrediction extends TProcessor {
		
	def query(query:Instance):Instances =  {
		return null;
	}
	
	def result(result:Instances, query:Instance) = {}

	
	def configure(properties: Properties) = {
		val normalizeProp = properties.getProperty(PROP_NORMALIZE);
		if(normalizeProp!=null){
		  normalize = normalizeProp.toBoolean
		}
		val minValueProp = properties.getProperty(PROP_MINVALUE);
		if(minValueProp!=null){
		  try{
		    dataMinValue = Integer.valueOf(minValueProp)		    
		  }
		  catch{
		    case nfe:NumberFormatException => 
		  }
		}
		val maxValueProp = properties.getProperty(PROP_MAXVALUE);
		if(minValueProp!=null){
		  try{
		    dataMaxValue = Integer.valueOf(maxValueProp)		    
		  }
		  catch{
		    case nfe:NumberFormatException => 
		  }
		}
		val normalizationRangeProp = properties.getProperty(PROP_NORMALIZATIONRANGE);
		if(normalizationRangeProp!=null){
		  try{
		    normalizationRange = java.lang.Double.parseDouble(normalizationRangeProp)		    
		  }
		  catch{
		    case nfe:NumberFormatException => 
		  }
		}
	} 

		
	def build (input:Instances) {
	 
		//detemine the amount of numeric attributes
		var numNumericAtts:Int = 0;  
		for(i <- 0 to input.numAttributes-1){
		    if(input.attribute(i).`type` == Attribute.NUMERIC && input.classIndex!=i){
		      numNumericAtts += 1
		    }
		}
		
		val splittedInput:Map[String, Instances] = ResultsUtil.splitInstanceBySource(input);
		
		for(inst:Instances <- splittedInput.values){
			//retieve the data
			var data:Array[Array[Double]] = Array.ofDim[Double](numNumericAtts, inst.numInstances)
			for(i <- 0 until inst.numInstances){
				var index:Int = 0
				for(j <- 0 until inst.numAttributes){
					if(inst.get(i).attribute(j).`type` == Attribute.NUMERIC && inst.classIndex!=j){
					  data(index)(i) = inst.get(i).value(j)
					  index += 1
					}
				}			
			}
			
			//perform prediction
			var result:Array[Array[Double]] =  Array.ofDim[Double](numNumericAtts, inst.numInstances)
			for(i <- 0 until numNumericAtts){
			  if(normalize){
			    data(i) = normalizeData(data(i))
			  }
			  result(i) = predictPeaks(data(i))
			}
			
			//update input
			for(i <- 0 until inst.numInstances){
				var index:Int = 0
				for(j <- 0 until inst.numAttributes){
					if(inst.get(i).attribute(j).`type` == Attribute.NUMERIC && inst.classIndex!=j){
					  inst.get(i).setValue(j,data(index)(i))
					  index += 1
					}
				}			
			}
		}
		
		val result:Instances = ResultsUtil.appendInstances(input.stringFreeStructure,splittedInput.values.toList);
		
		sendEvent(new Results(result));
	}

	def normalizeData(data:Array[Double]):Array[Double] = {
	  val mean:Double = (Math.abs(dataMinValue) + Math.abs(dataMaxValue)) / 2.0;
	  val dif:Double = mean - dataMaxValue;
	  
	  for(i <- 0 to data.length-1){
	    data(i) = ((data(i) + dif) / mean) * normalizationRange;
	  }
	  data
	}
	
	def predictPeaks(data:Array[Double]):Array[Double] = {
	
	  val maxValue:Double = if(normalize){normalizationRange}else{dataMaxValue};
	  var j:Int = 0;
	  
	  while(j < (data.size -1)){
	    
		var value:Double = data(j);
		
		if(Math.abs(value)==maxValue){
			var count:Int = 0;					
			while((j+count) < data.length && value==data(j+count)){
				count +=1;						
			}
			
			if(count>=2){
				var start:Int = j;
				var end:Int = j + count -1;
				
				val beforeIndex:Int = Math.max(0, start-2);
				val afterIndex:Int = Math.min(end+2, data.length-1);
				var before:Double = 0.0;
				var after:Double = 0.0;
				
				if(start!=beforeIndex){
					for(b <- beforeIndex to (start-1)){
						 before += data(b+1) - data(b);
					}
					before /= start-beforeIndex;
				}

				if(end!=afterIndex){
					for(a <- (end+1) to afterIndex){
						 after += data(a-1) - data(a);
					}
					after /= afterIndex-end;
				}

				
				val d:Double = (before+after)/2;						
				
				for(k <- 1 to ((count/2)-1)){
					data(start + k) += Math.sqrt(k)*d;
					data(end - k) +=  Math.sqrt(k)*d;
					if(k==((count/2)-1)){
						if(Math.abs(before)>Math.abs(after)){
							data(end-k) += d;
						}
						else{
							data(start+k) += d;
						}
					}
				}
				if(count%2!=0){
					data(start + ((count/2))) +=  Math.sqrt(count/2)*d;
				}
				if(count==2){
					if(Math.abs(before)>Math.abs(after)){
						data(end) += d;
					}
					else{
						data(start) += d;
					}
				}
			}
			j += count;
		}
		else{
			j += 1;
		}
	  }
	  
	  data
	}
}

object TruncatedPeakPrediction{
  val PROP_NORMALIZE = "normalize"
  val PROP_MINVALUE = "minValue"
  val PROP_MAXVALUE = "maxValue"
  val PROP_NORMALIZATIONRANGE = "normalizationrange"
  
  var normalize:Boolean = false
  var dataMinValue:Int = -128
  var dataMaxValue:Int = 127
  var normalizationRange:Double = 2.0 //normalize values to plus/minus normalizationRange  
}

class TruncatedPeakPredictionFactory extends TFactory{
 
  val name: String = TruncatedPeakPredictionFactory.name
  val id: String = TruncatedPeakPredictionFactory.id

  def getInstance: ActorRef = actorOf[TruncatedPeakPrediction]
  
  def createDefaultProperties: Properties = {
    val returns = new Properties
    returns setProperty (PROP_NORMALIZE, "false")
    returns setProperty (PROP_MINVALUE, "-128")
    returns setProperty (PROP_MAXVALUE, "127")
    returns setProperty (PROP_NORMALIZATIONRANGE, "2.0")
    returns
  }
  
  def createPropertyValues: Map[String, Array[_ <: Any]] = {
    Map()
  }

  def createPropertyDescription: Map[String, String] = {
    Map(PROP_NORMALIZE -> "normalize the input data to a specified range",
      PROP_MINVALUE -> "minimum value of the dataset",
      PROP_MAXVALUE -> "maximum value of the dataset",
      PROP_NORMALIZATIONRANGE -> "normalize values to plus/minus this value")
  }
   
}

object TruncatedPeakPredictionFactory{
	val name:String = "TruncatedPeakPrediction" 
    val id:String = classOf[TruncatedPeakPrediction].getName
}