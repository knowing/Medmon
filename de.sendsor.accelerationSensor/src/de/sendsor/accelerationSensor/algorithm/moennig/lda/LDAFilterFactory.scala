package de.sendsor.accelerationSensor.algorithm.moennig.lda

import java.util.Properties
import scala.collection.immutable.Map
import akka.actor.Actor
import de.lmu.ifi.dbs.knowing.core.factory._
import de.lmu.ifi.dbs.knowing.core.factory.TFactory._
import de.lmu.ifi.dbs.knowing.core.weka.{ WekaFilter, WekaFilterFactory }
import de.lmu.ifi.dbs.knowing.core.weka.WekaFilterFactory._
import LDAFilterFactory._
import weka.core.Instances

class LDAFilterFactory extends WekaFilterFactory[LDAFilterWrapper, LDAFilter](classOf[LDAFilterWrapper], classOf[LDAFilter]) {

  //Creates default Properties which are used if properties aren't set
  override def createDefaultProperties: Properties = {
    val returns = new Properties
    returns.setProperty(DEBUG, "false")
    returns.setProperty(DIMREDUCTION, "false")
    returns.setProperty(ATTRIBUTE_NAME_PREFIX, "FV_");
    returns
  }

  //Possible property values. Editor should show these
  override def createPropertyValues: Map[String, Array[_ <: Any]] = {
    Map(DEBUG -> boolean_property)
    Map(DIMREDUCTION -> boolean_property)
  }

  //Property description
  override def createPropertyDescription: Map[String, String] = {
    Map(DEBUG -> "Debug true/false")
    Map(DIMREDUCTION -> "Dimension reduction true/false")
  }

}

object LDAFilterFactory {
  val DIMREDUCTION = "dimensionreduction"
  val OUTDIMENSIONS = "outputdimensions"
  val ATTRIBUTE_NAME_PREFIX = "attributeNamePrefix"
}

class LDAFilterWrapper extends WekaFilter(new LDAFilter) {

  //LDA must be trained, before queries are being answered
  isBuild = false

  override def build(instances: Instances) {
    akka.event.EventHandler.debug(this, "Train LDA")
    guessAndSetClassLabel(instances)
    val lda = filter.asInstanceOf[LDAFilter]
    val header = new Instances(instances, 0)
    filter.setInputFormat(header)
    lda.train(instances)
    isBuild = true
    processStoredQueries
  }

  override def configure(properties: Properties) = {
    //Configure your classifier here with
    val lda = filter.asInstanceOf[LDAFilter]
    val debug = properties.getProperty(DEBUG)
    val dimreduction = properties.getProperty(DIMREDUCTION)
    val outdimensions = properties.getProperty(OUTDIMENSIONS)
    val attNamePrefix = properties.getProperty(ATTRIBUTE_NAME_PREFIX);
    lda.setDebug(debug.toBoolean)
    lda.setDimensionReduction(dimreduction.toBoolean)
    if (outdimensions != null) {
      lda.setOutDimensions(outdimensions.toInt)
    }
    lda.setAttributeNamePrefix(attNamePrefix)
  }
}