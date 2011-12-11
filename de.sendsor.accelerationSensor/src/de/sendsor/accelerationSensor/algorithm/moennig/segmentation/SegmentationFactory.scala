package de.sendsor.accelerationSensor.algorithm.moennig.segmentation

import java.util.Properties
import scala.collection.immutable.Map
import akka.actor.Actor
import de.lmu.ifi.dbs.knowing.core.factory._
import de.lmu.ifi.dbs.knowing.core.factory.TFactory._
import de.lmu.ifi.dbs.knowing.core.weka.{WekaFilter, WekaFilterFactory }
import de.lmu.ifi.dbs.knowing.core.weka.WekaFilterFactory._
import SegmentationFactory._
import de.lmu.ifi.dbs.knowing.core.japi.JProcessor
import akka.actor.ActorRef
import akka.actor.Actor.actorOf

class SegmentationFactory extends TFactory {
  
  val id = SegmentationFactory.id // id from static field
  val name = SegmentationFactory.name // name from static field
  
  def getInstance(): ActorRef = actorOf[SegmentationWrapper]
  
  //Creates default Properties which are used if properties aren't set
  override def createDefaultProperties: Properties = {
    val returns = new Properties
    returns.setProperty(DEBUG, "false")
    returns.setProperty(MIN_CORRELATION, "0.75")
    returns.setProperty(PATTERNSIZE, "25")
    returns.setProperty(MIN_SEGMENT_LENGTH, "100")
    returns.setProperty(SHIFTSAMPLE_LENGTH, "100")
    returns.setProperty(MIN_ATTRIBUTES_WITH_SEGMENTS, "1")
    
    returns
  }

  //Possible property values. Editor should show these
  override def createPropertyValues: Map[String, Array[_<:Any]] = {
    Map(DEBUG -> BOOLEAN_PROPERTY)        
  }

  //Property description
  override def createPropertyDescription: Map[String, String] = {
    	Map(DEBUG -> "Debug true/false")
    	Map(MIN_CORRELATION -> "Only correlation values above the minimum correlation are regared as periodically")
    	Map(PATTERNSIZE -> "Size of the sample window used for calculation of the correlation value")
    	Map(MIN_SEGMENT_LENGTH -> "Minimum amount of periodical samples needed for a valid segment")
    	Map(SHIFTSAMPLE_LENGTH -> "Amount of samples for shifting the pattern to determine the best correlation value")
    	Map(MIN_ATTRIBUTES_WITH_SEGMENTS -> "Amount of attributes of input data that have to satisfy the min correlation")
  }

}

object SegmentationFactory{
    val id = classOf[Segmentation].getName
    val name = "Segmentation"
  
	val MIN_CORRELATION = "minCorrelation"
	val PATTERNSIZE = "patternSize"
	val MIN_SEGMENT_LENGTH = "minSegmentLength"
	val SHIFTSAMPLE_LENGTH = "shiftSampleLength"
	val MIN_ATTRIBUTES_WITH_SEGMENTS = "minAttributesWithSegments"
	  
	val SEGMENTS = "segments"
	val NONSEGMENTS = "nonsegments"
}



class SegmentationWrapper extends JProcessor {
  
  //processor field is abstract and must be implemented
  val processor = new Segmentation(this) {
  
   override def configure(properties:Properties) = {
     //Configure your classifier here with
     val segmentation = this.asInstanceOf[Segmentation]
     //val debug = properties.getProperty(DEBUG)
     val minCorrelation = properties.getProperty(MIN_CORRELATION, "0.75")
     val patternSize = properties.getProperty(PATTERNSIZE, "25")
     val minSegmentLength = properties.getProperty(MIN_SEGMENT_LENGTH, "100")
     val shiftSampleLength = properties.getProperty(SHIFTSAMPLE_LENGTH, "100")
     val minAttributesWithSegments = properties.getProperty(MIN_ATTRIBUTES_WITH_SEGMENTS, "1")
     //segmentation.setDebug(debug.toBoolean)     
     segmentation.setMinCorrelation(minCorrelation.toDouble)
     segmentation.setPatternSize(patternSize.toInt)
     segmentation.setMinSegmentLength(minSegmentLength.toInt)
     segmentation.setShiftSampleLength(shiftSampleLength.toInt)
     segmentation.setMinAttributesWithSegments(minAttributesWithSegments.toInt)
   }
  }
}