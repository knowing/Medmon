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
    val path = TLoader.getFilePath(properties)
    converter.setFile(new File(path));
    
    val average = properties.getProperty(AGGREGATE)
    if(AGGREGATE_PROPERTIES contains(average)) {
      converter.setAggregate(average)
    }
    
    val interval = properties.getProperty(INTERVAL)
    if(INTERVAL_PROPERTIES contains(interval)) {
      converter.setInterval(interval)
    }
    
    val units = properties.getProperty(UNITS)
    converter.setUnits(units.toDouble)
    
    val timestamp = properties.getProperty(RELATIVE_TIMESTAMP)
    timestamp match {
      case RELATIVE_TIMESTAMP_ABSOLUTE => converter.setRelativeTimestamp(false)
      case RELATIVE_TIMESTAMP_RELATIVE => converter.setRelativeTimestamp(true)
      case _ => converter.setRelativeTimestamp(false) //Print warning
    }
    
    val output = properties.getProperty(OUTPUT)
    converter.setOutput(output)
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
    props.setProperty(UNITS, "1")
    props.setProperty(RELATIVE_TIMESTAMP,  RELATIVE_TIMESTAMP_ABSOLUTE)
    props
  }

  def createPropertyValues(): Map[String, Array[Any]] = {
    Map(AGGREGATE -> AGGREGATE_PROPERTIES.toArray ,
        INTERVAL -> INTERVAL_PROPERTIES.toArray ,
        RELATIVE_TIMESTAMP -> Array( RELATIVE_TIMESTAMP_RELATIVE,  RELATIVE_TIMESTAMP_ABSOLUTE))
  }

  def createPropertyDescription(): Map[String, String] = {
    Map(AGGREGATE -> "Aggregate: none|average|interval_first|interval_last .",
        INTERVAL -> "In which interval should the data be aggregated: second|minute|hour|day",
        UNITS -> "Must be greater than 0. Determines the interval size (units*interval). E.g 15 minutes",
        RELATIVE_TIMESTAMP -> "relative | absolute .Relative uses the first timestamp and assumes correct recorded data")
  }

}

object SDRLoaderFactory {
  val name: String = "SDR Loader"
  val id: String = classOf[SDRLoader].getName

  val AGGREGATE = "aggregate"
  val INTERVAL = "interval"
  val UNITS = "units"
  val RELATIVE_TIMESTAMP = "timestamp"
  val OUTPUT = "output"

  val AGGREGATE_NONE = "none"
  val AGGREGATE_AVERAGE = "average" //Default
  val AGGREGATE_INTERVAL_FIRST = "interval_first"
  val AGGREGATE_INTERVAL_LAST = "interval_last"
  val AGGREGATE_PROPERTIES = Array(AGGREGATE_NONE, AGGREGATE_AVERAGE, AGGREGATE_INTERVAL_FIRST, AGGREGATE_INTERVAL_LAST)
    
  val INTERVAL_SECOND = "second" //Default
  val INTERVAL_MINUTE = "minute"
  val INTERVAL_HOUR = "hour"
  val INTERVAL_DAY = "day"
  val INTERVAL_PROPERTIES = Array(INTERVAL_SECOND, INTERVAL_MINUTE, INTERVAL_HOUR, INTERVAL_DAY)
  
  val RELATIVE_TIMESTAMP_RELATIVE = "relative"
  val RELATIVE_TIMESTAMP_ABSOLUTE = "absolute" //Default
}