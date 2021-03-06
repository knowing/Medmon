package de.sendsor.accelerationSensor.algorithm.moennig.preprocessing

import SourceToClassConverter._
import java.util.Properties
import de.lmu.ifi.dbs.knowing.core.events.Progress
import de.lmu.ifi.dbs.knowing.core.processing.TProcessor
import de.lmu.ifi.dbs.knowing.core.factory.ProcessorFactory
import de.lmu.ifi.dbs.knowing.core.results.ResultsType.{ATTRIBUTE_SOURCE,ATTRIBUTE_CLASS}
import akka.actor.ActorRef
import weka.core.{ Instance, Instances, Attribute }
import weka.core.FastVector

/**
 * Reads the class token from the filename store inside 
 * the ATTRIBUTE_SOURCE attribute.
 * 
 * 
 * @author Christian Moenning
 * @version 1.0
 */
class SourceToClassConverter extends TProcessor {

	def configure(properties: Properties) = {
		val delimiterProp = properties.getProperty(PROP_DELIMITER);
		if (delimiterProp != null) {
			delimiter = delimiterProp.toCharArray()(0)
		}
		val positionProp = properties.getProperty(PROP_POSITION);
		if (positionProp != null) {
			try {
				position = Integer.valueOf(positionProp)
			} catch {
				case nfe: NumberFormatException =>
			}
		}
	}

	def process(input: Instances) = {
		case _ =>
			statusChanged(Progress("Copy input", 0, 3))
			val result: Instances = new Instances(input);

			var sourcePos: Int = -1
			for (i <- 0 until result.numAttributes()) {
				if (result.attribute(i).name().equals(ATTRIBUTE_SOURCE) && result.attribute(i).isNominal) {
					sourcePos = i
				}
			}

			if (sourcePos >= 0) {
				statusChanged(Progress("Read class Attribute", 1, 4))
				val classes = new FastVector[String]()
				for (i <- 0 until result.attribute(sourcePos).numValues) {
					val cla: String = result.attribute(sourcePos).value(i).split(delimiter)(position)
					if (!classes.contains(cla)) {
						classes.add(cla)
					}
				}
				statusChanged(Progress("Insert attribute", 2, 4))
				result.insertAttributeAt(new Attribute(ATTRIBUTE_CLASS, classes), result.numAttributes)
				result.setClassIndex(result.numAttributes - 1)
				statusChanged(Progress("Insert class value", 3, 4))
				for (i <- 0 until result.numInstances) {
					val cla: String = result.get(i).stringValue(sourcePos).split(delimiter)(position)
					result.get(i).setClassValue(cla)
				}
			}
			statusChanged(new Progress("Finished", 4, 4))
			sendResults(result)
	}

	def query(query: Instances): Instances = throw new UnsupportedOperationException
}

object SourceToClassConverter {
	val PROP_DELIMITER = "delimiter"
	val PROP_POSITION = "position"

	var delimiter: Char = '-'
	var position: Int = 1
}

class SourceToClassConverterFactory extends ProcessorFactory(classOf[SourceToClassConverter]) {

	override def createDefaultProperties: Properties = {
		val returns = new Properties
		returns setProperty (PROP_DELIMITER, "-")
		returns setProperty (PROP_POSITION, "2")

		returns
	}

	override def createPropertyDescription: Map[String, String] = {
		Map(PROP_DELIMITER -> "delimiter char for splitting the source name",
			PROP_POSITION -> "split position of the class name (starting with 0)")
	}

}