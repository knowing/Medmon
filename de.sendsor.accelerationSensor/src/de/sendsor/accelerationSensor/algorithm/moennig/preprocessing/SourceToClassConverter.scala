package de.sendsor.accelerationSensor.algorithm.moennig.preprocessing

import SourceToClassConverter._
import SourceToClassConverterFactory._
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
import weka.core.FastVector

class SourceToClassConverter extends TProcessor {
		
	def query(query:Instance):Instances =  {
		return null;
	}
	
	def result(result:Instances, query:Instance) = {}

	
	def configure(properties: Properties) = {
		val delimiterProp = properties.getProperty(PROP_DELIMITER);
		if(delimiterProp!=null){
		  delimiter = delimiterProp.toCharArray()(0)
		}
		val positionProp = properties.getProperty(PROP_POSITION);
		if(positionProp!=null){
		  try{
		    position = Integer.valueOf(positionProp)		    
		  }
		  catch{
		    case nfe:NumberFormatException => 
		  }
		}		
	} 

		
	override def build (input:Instances) {
		
		val result:Instances = new Instances(input);
		
		var sourcePos:Int = -1;
		for(i <- 0 until result.numAttributes()){
		  if(result.attribute(i).name().equals(ResultsUtil.ATTRIBUTE_SOURCE) && result.attribute(i).isNominal){
		    sourcePos = i;		    
		  }
		}
				
	  
		if(sourcePos>=0){
			val classes:FastVector[String] = new FastVector();
			for(i <- 0 until result.attribute(sourcePos).numValues()){
			  val cla:String = result.attribute(sourcePos).value(i).split(delimiter)(position);
			  if(!classes.contains(cla)){
				classes.add(cla);
			  }
			}
			result.insertAttributeAt(new Attribute("class", classes), result.numAttributes());
			result.setClassIndex(result.numAttributes-1);
			for(i <- 0 until result.numInstances()){
			  val cla:String = result.get(i).stringValue(sourcePos).split(delimiter)(position);
			  result.get(i).setClassValue(cla);
			}
		}
		sendEvent(new Results(result));
	}
}

object SourceToClassConverter{
  val PROP_DELIMITER = "delimiter"
  val PROP_POSITION = "position"  
  
  var delimiter:Char = '-'
  var position:Int = 1  
}

class SourceToClassConverterFactory extends TFactory{
 
  val name: String = SourceToClassConverterFactory.name
  val id: String = SourceToClassConverterFactory.id

  def getInstance: ActorRef = actorOf[SourceToClassConverter]
  
  def createDefaultProperties: Properties = {
    val returns = new Properties
    returns setProperty (PROP_DELIMITER, "-")
    returns setProperty (PROP_POSITION, "2")
    
    returns
  }
  
  def createPropertyValues: Map[String, Array[_ <: Any]] = {
    Map()
  }

  def createPropertyDescription: Map[String, String] = {
    Map(PROP_DELIMITER -> "delimiter char for splitting the source name",
      PROP_POSITION -> "split position of the class name (starting with 0)")
  }
   
}

object SourceToClassConverterFactory{
	val name:String = "SourceToClassConverter" 
    val id:String = classOf[SourceToClassConverter].getName
}