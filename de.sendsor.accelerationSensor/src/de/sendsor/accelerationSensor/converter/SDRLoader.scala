package de.sendsor.accelerationSensor.converter

import akka.actor.ActorRef
import akka.actor.Actors.actorOf

import scala.collection.immutable.Map
import java.util.Properties
import java.io.File

import de.lmu.ifi.dbs.knowing.core.factory.TFactory
import de.lmu.ifi.dbs.knowing.core.processing.TLoader
import de.lmu.ifi.dbs.knowing.core.processing.TLoader._
import de.sendsor.accelerationSensor.converter.SDRLoaderFactory._

import weka.core.Instances

/**
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 08.05.2011
 *
 */
class SDRLoader extends TLoader {

  val converter = new SDRConverter

  def getDataSet(): Instances = converter.getDataSet

  def configure(properties: Properties) = {
    val path = properties.getProperty(FILE)
    converter.setFile(new File(path));
    
    val average = properties.getProperty(AGGREGATE)
    if(AGGREGATE_PROPERTIES contains(average)) {
      converter.setAggregate(average)
    }
    
    val interval = properties.getProperty(INTERVAL)
    if(INTERVAL_PROPERTIES contains(interval)) {
      converter.setInterval(interval)
    }
  }

  def reset = converter.reset
}

class SDRLoaderFactory extends TFactory {

  val name: String = SDRLoaderFactory.name
  val id: String = SDRLoaderFactory.id

  def getInstance(): ActorRef = actorOf(classOf[SDRLoader])

  def createDefaultProperties(): Properties = {
    val props = new Properties
    props.setProperty(FILE, "")
    props.setProperty(URL, "")
    props.setProperty(AGGREGATE, AGGREGATE_AVERAGE )
    props.setProperty(INTERVAL, INTERVAL_SECOND)
    props
  }

  def createPropertyValues(): Map[String, Array[Any]] = Map()

  def createPropertyDescription(): Map[String, String] = Map()

}

object SDRLoaderFactory {
  val name: String = "SDR Loader"
  val id: String = classOf[SDRLoader].getName

  val AGGREGATE = "average"
  val INTERVAL = "interval"

  val AGGREGATE_NONE = "none"
  val AGGREGATE_AVERAGE = "average"
  val AGGREGATE_INTERVAL_FIRST = "interval_first"
  val AGGREGATE_INTERVAL_LAST = "interval_last"
  val AGGREGATE_PROPERTIES = Array(AGGREGATE_NONE, AGGREGATE_AVERAGE, AGGREGATE_INTERVAL_FIRST, AGGREGATE_INTERVAL_LAST)
    
  val INTERVAL_SECOND = "second"
  val INTERVAL_MINUTE = "minute"
  val INTERVAL_HOUR = "hour"
  val INTERVAL_DAY = "day"
  val INTERVAL_PROPERTIES = Array(INTERVAL_SECOND, INTERVAL_MINUTE, INTERVAL_HOUR, INTERVAL_DAY)
}