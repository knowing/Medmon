package de.sendsor.accelerationSensor.algorithm.moennig.segmentation

import java.util.Properties
import scala.collection.immutable.Map
import akka.actor.Actor
import de.lmu.ifi.dbs.knowing.core.factory._
import de.lmu.ifi.dbs.knowing.core.factory.TFactory._
import de.lmu.ifi.dbs.knowing.core.weka.{WekaFilter, WekaFilterFactory }
import de.lmu.ifi.dbs.knowing.core.weka.WekaFilterFactory._
import SegmentationFactory._

class SegmentationFactory extends WekaFilterFactory[SegmentationWrapper, Segmentation](classOf[SegmentationWrapper], classOf[Segmentation]) {
  
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
    Map(DEBUG -> boolean_property)        
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
	val MIN_CORRELATION = "minCorrelation"
	val PATTERNSIZE = "patternSize"
	val MIN_SEGMENT_LENGTH = "minSegmentLength"
	val SHIFTSAMPLE_LENGTH = "shiftSampleLength"
	val MIN_ATTRIBUTES_WITH_SEGMENTS = "minAttributesWithSegments"
}

class SegmentationWrapper extends WekaFilter(new Segmentation()) {
  
   override def configure(properties:Properties) = {
     //Configure your classifier here with
     val segmentation = filter.asInstanceOf[Segmentation]
     val debug = properties.getProperty(DEBUG)
     val minCorrelation = properties.getProperty(MIN_CORRELATION)
     val patternSize = properties.getProperty(PATTERNSIZE)
     val minSegmentLength = properties.getProperty(MIN_SEGMENT_LENGTH)
     val shiftSampleLength = properties.getProperty(SHIFTSAMPLE_LENGTH)
     val minAttributesWithSegments = properties.getProperty(MIN_ATTRIBUTES_WITH_SEGMENTS)
     segmentation.setDebug(debug.toBoolean)     
     segmentation.setMinCorrelation(minCorrelation.toDouble)
     segmentation.setPatternSize(patternSize.toInt)
     segmentation.setMinSegmentLength(minSegmentLength.toInt)
     segmentation.setShiftSampleLength(shiftSampleLength.toInt)
     segmentation.setMinAttributesWithSegments(minAttributesWithSegments.toInt)
   }
}