package de.sendsor.accelerationSensor

import akka.actor.ActorRef
import akka.actor.Actors.actorOf

import scala.collection.immutable.Map
import java.util.Properties
import java.io.File

import de.lmu.ifi.dbs.knowing.core.factory.ProcessorFactory
import de.lmu.ifi.dbs.knowing.core.processing.TLoader
import de.lmu.ifi.dbs.knowing.core.processing.TLoader._
import de.sendsor.accelerationSensor.SDRLoaderFactory._

import weka.core.Instances

/**
 * @author Nepomuk Seiler
 * @version 0.1.1
 * @since 08.05.2011
 *
 */
class SDRLoader extends TLoader {

  private[this] var average = AGGREGATE_NONE
  private[this] var interval = INTERVAL_SECOND
  private[this] var units = 1.0
  private[this] var timestamp = RELATIVE_TIMESTAMP_RELATIVE

  def getDataSet(): Instances = {
    val converter = new SDRConverter(inputs.values.head)
    converter.setAggregate(average)
    converter.setInterval(interval)
    converter.setUnits(units.toDouble)
    timestamp match {
      case RELATIVE_TIMESTAMP_ABSOLUTE => converter.setRelativeTimestamp(false)
      case RELATIVE_TIMESTAMP_RELATIVE => converter.setRelativeTimestamp(true)
      case _ => converter.setRelativeTimestamp(true) //Print warning
    }

    converter.getData
  }

  def configure(properties: Properties) {

    val average = properties.getProperty(AGGREGATE, AGGREGATE_NONE)
    if (AGGREGATE_PROPERTIES contains (average)) {
      this.average = average
    }

    val interval = properties.getProperty(INTERVAL, INTERVAL_SECOND)
    if (INTERVAL_PROPERTIES contains (interval)) {
      this.interval = interval
    }
    val units = properties.getProperty(UNITS, "1.0")
    val timestamp = properties.getProperty(RELATIVE_TIMESTAMP)

  }

  def reset = {}
}

class SDRLoaderFactory extends ProcessorFactory(classOf[SDRLoader]) {

  override def createDefaultProperties(): Properties = {
    val props = new Properties
    props.setProperty(FILE, "")
    props.setProperty(URL, "")
    props.setProperty(AGGREGATE, AGGREGATE_NONE)
    props.setProperty(INTERVAL, INTERVAL_SECOND)
    props.setProperty(UNITS, "1")
    props.setProperty(RELATIVE_TIMESTAMP, RELATIVE_TIMESTAMP_ABSOLUTE)
    props
  }

  override def createPropertyValues(): Map[String, Array[Any]] = {
    Map(AGGREGATE -> AGGREGATE_PROPERTIES.toArray,
      INTERVAL -> INTERVAL_PROPERTIES.toArray,
      RELATIVE_TIMESTAMP -> Array(RELATIVE_TIMESTAMP_RELATIVE, RELATIVE_TIMESTAMP_ABSOLUTE))
  }

  override def createPropertyDescription(): Map[String, String] = {
    Map(AGGREGATE -> "Aggregate: none|average|interval_first|interval_last .",
      INTERVAL -> "In which interval should the data be aggregated: second|minute|hour|day",
      UNITS -> "Must be greater than 0. Determines the interval size (units*interval). E.g 15 minutes",
      RELATIVE_TIMESTAMP -> "relative | absolute .Relative uses the first timestamp and assumes correct recorded data")
  }

}

object SDRLoaderFactory {

  val AGGREGATE = "aggregate"
  val INTERVAL = "interval"
  val UNITS = "units"
  val RELATIVE_TIMESTAMP = "timestamp"

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