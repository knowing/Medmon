package de.sendsor.accelerationSensor.algorithm.moennig.fv

import java.util.Properties
import scala.collection.immutable.Map
import akka.actor.Actor
import de.lmu.ifi.dbs.knowing.core.factory._
import de.lmu.ifi.dbs.knowing.core.factory.TFactory._
import de.lmu.ifi.dbs.knowing.core.weka.{WekaFilter, WekaFilterFactory }
import de.lmu.ifi.dbs.knowing.core.weka.WekaFilterFactory._
import AugmentedFVFactory._

class AugmentedFVFactory extends WekaFilterFactory[AugmentedFVWrapper, AugmentedFV](classOf[AugmentedFVWrapper], classOf[AugmentedFV]) {
  
  //Creates default Properties which are used if properties aren't set
  override def createDefaultProperties: Properties = {
    val returns = new Properties
    returns.setProperty(DEBUG, "false")
    returns.setProperty(USE_AR, "true")
    returns.setProperty(USE_SMA, "true")
    returns.setProperty(USE_TA, "true")
    returns.setProperty(USE_APA, "true")
    returns.setProperty(USE_SSR, "true")
    returns.setProperty(USE_MV, "false")
    returns.setProperty(USE_IAC, "true")
    returns
  }

  //Possible property values. Editor should show these
  override def createPropertyValues: Map[String, Array[_<:Any]] = {
    Map(DEBUG -> boolean_property)
    Map(USE_AR -> boolean_property)
    Map(USE_SMA -> boolean_property)
    Map(USE_TA -> boolean_property)
    Map(USE_APA -> boolean_property)
    Map(USE_SSR -> boolean_property)
    Map(USE_MV -> boolean_property)
    Map(USE_IAC -> boolean_property)
  }

  //Property description
  override def createPropertyDescription: Map[String, String] = {
    Map(DEBUG -> "Debug true/false")
    Map(USE_AR -> "use auto regressive coefficients true/false")
    Map(USE_SMA -> "use signal magnitude area true/false")
    Map(USE_TA -> "use tilt angle true/false")
    Map(USE_APA -> "use average peak amplitude true/false")
    Map(USE_SSR -> "use surrounding segmentation rate true/false")
    Map(USE_MV -> "use mean and variance true/false")
    Map(USE_IAC -> "use the inter axis correlation true/false")
  }

}

object AugmentedFVFactory{
  val USE_AR = "useAR";
  val USE_SMA = "useSMA";
  val USE_TA = "useTA";
  val USE_APA = "useAPA";
  val USE_SSR = "useSSR";
  val USE_MV = "useMV";  	
  val USE_IAC = "useIAC";
}

class AugmentedFVWrapper extends WekaFilter(new AugmentedFV()) {
  
   override def configure(properties:Properties) = {
     //Configure your classifier here with
     val augmentedFV = filter.asInstanceOf[AugmentedFV]
     val debug = properties.getProperty(DEBUG)
     val useAR = properties.getProperty(USE_AR)
     val useSMA = properties.getProperty(USE_SMA)
     val useTA = properties.getProperty(USE_TA)
     val useAPA = properties.getProperty(USE_APA)
     val useSSR = properties.getProperty(USE_SSR)
     val useMV = properties.getProperty(USE_MV)
     val useIAC = properties.getProperty(USE_IAC)
     augmentedFV.setDebug(debug.toBoolean)
     augmentedFV.setUseAR(useAR.toBoolean)
     augmentedFV.setUseSMA(useSMA.toBoolean)
     augmentedFV.setUseTA(useTA.toBoolean)
     augmentedFV.setUseAPA(useAPA.toBoolean)
     augmentedFV.setUseSSR(useSSR.toBoolean)
     augmentedFV.setUseMV(useMV.toBoolean)
     augmentedFV.setUseIAC(useIAC.toBoolean)
   }
}