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
    returns
  }

  //Possible property values. Editor should show these
  override def createPropertyValues: Map[String, Array[_<:Any]] = {
    Map(DEBUG -> boolean_property)    
  }

  //Property description
  override def createPropertyDescription: Map[String, String] = {
        Map(DEBUG -> "Debug true/false")                
  }

}

object SegmentationFactory{
  
}



class SegmentationWrapper extends WekaFilter(new Segmentation()) {
  
   override def configure(properties:Properties) = {
     //Configure your classifier here with
     val myFilter = filter.asInstanceOf[Segmentation]
     val debug = properties.getProperty(DEBUG)
     myFilter.setDebug(debug.toBoolean)     
   }
}