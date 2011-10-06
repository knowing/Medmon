package de.sendsor.accelerationSensor.algorithm.presentation

import java.util.Properties
import akka.actor.ScalaActorRef
import weka.core.{Instance, Instances , Attribute }
import de.lmu.ifi.dbs.knowing.core.processing.TFilter
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil

import scala.collection.JavaConversions._

class AddTimestamp extends TFilter {

  private var labels: List[String] = Nil
  private var relAttr = -1
  private var timeAttr = -1
  private var firstRun = true
  
  def query(query: Instance): Instances = { 
    val header = query.dataset
    val classIndex = guessAndSetClassLabel(header)
    firstRun match {
      case true =>
        labels = header.classAttribute.enumerateValues.toList.map(_.asInstanceOf[String])
        //Find relation attribute
        for(i <- 0 until header.numAttributes) {
          header.attribute(i).`type` match {
            case Attribute.RELATIONAL => relAttr = header.attribute(i).index
            case _ => //do nothing
          }
        }
        //Find timestamp attribute in relation
        val relation = header.attribute(relAttr).relation
        for(i <- 0 until relation.numAttributes) {
             relation.attribute(i).`type` match {
            case Attribute.DATE => timeAttr = relation.attribute(i).index
            case _ => //do nothing
          }
        }
      case false => 
    }
    
    val result = ResultsUtil.timeIntervalResult(labels)
    val relation = header.attribute(relAttr).relation
    val clazz = header.classAttribute.value(query.value(classIndex).toInt)
    
    result
  }

  def result(result: Instances, query: Instance): Unit = {  }

  def configure(properties: Properties): Unit = {  }

}