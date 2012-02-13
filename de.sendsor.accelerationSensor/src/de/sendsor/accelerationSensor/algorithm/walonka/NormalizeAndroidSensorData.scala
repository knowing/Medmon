package de.sendsor.accelerationSensor.algorithm.walonka

import java.util.Properties
import weka.core.{ Instances, Attribute, DenseInstance, Instance }
import de.lmu.ifi.dbs.knowing.core.events._
import de.lmu.ifi.dbs.knowing.core.factory.ProcessorFactory
import de.lmu.ifi.dbs.knowing.core.processing.TFilter
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil.{ findValueAttributes, findNumericAttributes }
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil.ATTRIBUTE_TIMESTAMP
import scala.collection.JavaConversions._
import scala.collection.parallel.mutable.ParSeq
import NormalizeAndroidSensorDataFactory._

/**
 * Normalize sensor data to a specific frequency.
 * This processor works for ResultUtils.timeSeriesResult
 *
 * @author Nepomuk Seiler
 * @version 0.1
 */
class NormalizeAndroidSensorData extends TFilter {

	private var hz = 25
	private var strategy = STRATEGY_AVERAGE

	override def filter(instances: Instances): Instances = {
		statusChanged(Progress("Normalize with" + hz,0,4))
		val filtered = new Instances(instances, instances.size)
		val timeslot = 1000 / hz //in ms

		//careful! If dataset consist of a value y0, it is handled with the first call
		val valueAttrs = findValueAttributes(instances) match {
			case attr if attr.isEmpty => findNumericAttributes(instances).toList
			case attr => attr.toList
		}
		val timeAttr = instances.attribute(ATTRIBUTE_TIMESTAMP)

		statusChanged(Progress("Group timeslots",1,4))
		//run it with scala parallel collection
		val grouped = instances.par groupBy {
			inst =>
				val timestamp = inst.value(timeAttr).toLong
				val millis = timestamp % 1000
				val times = millis / timeslot
				val floor = times * timeslot
				val ceil = (times + 1) * timeslot

				val FloorVal = millis - floor
				val CeilVal = ceil - millis
				val nearest = Math.min(FloorVal, CeilVal)
				nearest match {
					case FloorVal => timestamp - FloorVal
					case CeilVal => timestamp + CeilVal
				}
		}
		statusChanged(Progress("Use " + strategy, 2,4))
		
		//average values -> extract in custom method
		grouped.par foreach {
			case (timestamp, instances) if instances.isEmpty => throw new Exception("Empty timeslot at " + timestamp)

			//only one value
			case (timestamp, instances) if instances.size == 1 =>
				val inst = new DenseInstance(instances.head)
				inst.setValue(timeAttr, timestamp)
				filtered.add(inst)

			//average | choose 
			case (timestamp, instances) =>
				val inst = strategy match {
					case STRATEGY_AVERAGE => average(instances, valueAttrs)
					case STRATEGY_FIRST => first(instances)
					case STRATEGY_LAST => last(instances)
				}
				inst.setValue(timeAttr, timestamp)
				filtered.add(inst)
		}
		statusChanged(Progress("Sort results", 3,4))
		//make sure it's sorted
		filtered.sort(timeAttr)
		
		statusChanged(Progress("Finished", 4,4))
		statusChanged(Ready())
		filtered
	}

	private def average(instances: ParSeq[Instance], attributes: List[Attribute]): Instance = {
		val inst = new DenseInstance(instances.head)
		for (attr <- attributes) {
			val sum = instances.foldLeft(0.0)((sum, inst) => sum + inst.value(attr))
			val avg = sum / instances.size
			inst.setValue(attr, avg)
		}
		inst
	}

	private def first(instances: ParSeq[Instance]): Instance = new DenseInstance(instances.head)

	private def last(instances: ParSeq[Instance]): Instance = new DenseInstance(instances.reverse.head)

	def configure(properties: Properties) = {
		hz = properties.getProperty(HZ, "25").toInt
		strategy = properties.getProperty(STRATEGY, STRATEGY_AVERAGE)
	}

	def query(query: Instance): Instances = throw new UnsupportedOperationException

	def result(result: Instances, query: Instance) = {}

}

class NormalizeAndroidSensorDataFactory extends ProcessorFactory(classOf[NormalizeAndroidSensorData]) {

	override def createDefaultProperties: Properties = {
		val props = new Properties
		props.setProperty(HZ, "25")
		props.setProperty(STRATEGY, STRATEGY_AVERAGE)
		props
	}

	override def createPropertyValues: Map[String, Array[_ <: Any]] = Map(
		STRATEGY -> Array(STRATEGY_AVERAGE, STRATEGY_FIRST, STRATEGY_LAST))

	override def createPropertyDescription: Map[String, String] = Map(
		HZ -> "Target frequency. Has to be lower than the source frequency",
		STRATEGY -> "How to deal with multiple values. Average | take first | take last")

}

object NormalizeAndroidSensorDataFactory {

	val HZ = "hz"
	val STRATEGY = "strategy"

	val STRATEGY_AVERAGE = "average"
	val STRATEGY_FIRST = "first"
	val STRATEGY_LAST = "last"
}
