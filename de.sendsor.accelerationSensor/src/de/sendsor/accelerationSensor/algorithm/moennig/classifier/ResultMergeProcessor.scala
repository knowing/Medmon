package de.sendsor.accelerationSensor.algorithm.moennig.classifier

import java.util.{ ArrayList, Arrays, Collections, List => JList, Properties, Map => JMap }
import scala.collection.JavaConversions._
import java.util.{Date,Properties}
import java.text.SimpleDateFormat
import de.lmu.ifi.dbs.knowing.core.factory.ProcessorFactory
import de.lmu.ifi.dbs.knowing.core.processing.TProcessor
import de.lmu.ifi.dbs.knowing.core.processing.ImmutableInstances
import de.lmu.ifi.dbs.knowing.core.events.Results
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil
import de.lmu.ifi.dbs.knowing.core.results.ClassDistributionResults.ATTRIBUTE_CLASS
import weka.core.{Instance,Instances, Attribute,DenseInstance, FastVector}
import ResultMergeProcessorFactory._

/**
 * Never touch this class.
 * 
 * @author Christian Moenning
 * @version 0.1
 */
class ResultMergeProcessor extends TProcessor {

	var segments: Instances = _
	var nonsegments: Instances = _
	var rawdata: Instances = _

	var inputRelIndex = -1
	var inputStartTime = -1

	var sdf: SimpleDateFormat = _
	var sdfRaw: SimpleDateFormat = _

	var labels: List[Any] = _

	override def process(inst: Instances) = {
		case (None, _) => log.warning("Unkown input in ResultMergeProcessor on default port")
		case (Some(INPUT_RAWDATA), _) => addRawdata(inst)
		case (Some(INPUT_SEGMENTS), _) => addSegments(inst)
		case (Some(INPUT_NONSEGMENTS), _) => addNonSegments(inst)
		case (Some(p), _) => log.warning("Unkown input in ResultMergeProcessor on port " + p)
	}

	def addSegments(segs: Instances) {
		segments = segs;
		if (segments != null && nonsegments != null && rawdata != null) {
			merge()
		}
	}

	def addNonSegments(nonSegs: Instances) {
		nonsegments = nonSegs;
		if (segments != null && nonsegments != null && rawdata != null) {
			merge()
		}
	}

	def addRawdata(rawd: Instances) {
		rawdata = rawd;
		if (segments != null && nonsegments != null && rawdata != null) {
			merge()
		}
	}

	def determineFormat(): Instances = {
		for (i <- 0 until segments.numAttributes) {
			if (segments.attribute(i).`type` == Attribute.RELATIONAL && inputRelIndex < 0) {
				inputRelIndex = i
			}
			if (segments.attribute(i).`type` == Attribute.DATE && inputStartTime < 0) {
				inputStartTime = i
				sdf = new SimpleDateFormat(segments.attribute(i).getDateFormat());
			}
		}

		val attributes = new FastVector[Attribute]
		val relaltionalAttribute: Instances = segments.attribute(inputRelIndex).relation()
		for (i <- 0 until relaltionalAttribute.numAttributes()) {
			attributes.addElement(relaltionalAttribute.attribute(i))
		}

		labels = segments.classAttribute.enumerateValues.toList
		labels foreach (l => attributes.add(new Attribute("class" + l)))

		new Instances("data", attributes, 0)
	}

	def merge() {

		val output = determineFormat()

		// Map[String,Instances]
		val splittedRawdata = ResultsUtil.splitInstanceBySource(rawdata, false)
		val splittedSegments = ResultsUtil.splitInstanceBySource(segments, false)
		val splittedNonSegments = ResultsUtil.splitInstanceBySource(nonsegments, false)

		for (key: String <- splittedRawdata.keys) {
			val raw = splittedRawdata(key)
			val segs = splittedSegments(key)
			val nonsegs = splittedNonSegments(key)

			segs.sort(inputStartTime)
			nonsegs.sort(inputStartTime)
			var si = 0
			var ni = 0
			var ri = 0

			sdfRaw = new SimpleDateFormat(raw.attribute(inputStartTime).getDateFormat());

			while (si < segs.numInstances || ni < nonsegs.numInstances) {
				if (si < segs.numInstances && ni < nonsegs.numInstances) {
					val sDate = sdf.parse(segs.get(si).stringValue(inputStartTime))
					val nDate = sdf.parse(nonsegs.get(ni).stringValue(inputStartTime))
					var rDate = sdfRaw.parse(raw.get(ri).stringValue(inputStartTime))
					while (rDate.before(sDate) && rDate.before(nDate)) {
						addRawInstnace(output, raw.get(ri))
						ri += 1
						rDate = sdfRaw.parse(raw.get(ri).stringValue(inputStartTime))
					}
					if (sDate.before(nDate)) {
						ri += addRelationalInstance(output, segs.get(si))
						si += 1
					} else {
						ri += addRelationalInstance(output, nonsegs.get(ni))
						ni += 1
					}
				} else if (si < segs.numInstances) {
					val sDate: Date = sdf.parse(segs.get(si).stringValue(inputStartTime))
					var rDate: Date = sdfRaw.parse(raw.get(ri).stringValue(inputStartTime))
					while (rDate.before(sDate)) {
						addRawInstnace(output, raw.get(ri))
						ri += 1
						rDate = sdfRaw.parse(raw.get(ri).stringValue(inputStartTime))
					}
					ri += addRelationalInstance(output, segs.get(si))
					si += 1
				} else {
					val nDate: Date = sdf.parse(nonsegs.get(ni).stringValue(inputStartTime))
					var rDate: Date = sdfRaw.parse(raw.get(ri).stringValue(inputStartTime))
					while (rDate.before(nDate)) {
						addRawInstnace(output, raw.get(ri))
						ri += 1
						rDate = sdfRaw.parse(raw.get(ri).stringValue(inputStartTime))
					}
					ri += addRelationalInstance(output, nonsegs.get(ni))
					ni += 1
				}
			}
			while (ri < raw.numInstances()) {
				addRawInstnace(output, raw.get(ri))
				ri += 1
			}
		}

		//Set class labels for output
		guessAndSetClassLabel(output)
		for (i <- 0 until output.numInstances) ResultsUtil.highestProbability(output(i))._2 match {
			case ResultsUtil.NOT_CLASSIFIED => output(i).setClassMissing
			case clazz => output(i).setClassValue(clazz)
		}

		sendResults(output)

		//Clean up
		segments = null
		nonsegments = null
		rawdata = null

		sdf = null
		sdfRaw = null
	}

	def addRelationalInstance(output: Instances, inst: Instance): Int = {
		val relAtt: Instances = inst.relationalValue(inputRelIndex);

		for (i <- 0 until relAtt.numInstances) {
			val result = new DenseInstance(output.numAttributes)
			for (j <- 0 until relAtt.get(i).numAttributes) {
				result.setValue(j, relAtt.get(i).value(j))
			}
			for (j <- 0 until labels.size) {
				result.setValue(j + relAtt.numAttributes, inst.value(inst.dataset.attribute(ATTRIBUTE_CLASS + labels.get(j))))
			}
			output.add(result)
		}
		return relAtt.numInstances
	}

	def addRawInstnace(output: Instances, inst: Instance) {
		val result = new DenseInstance(output.numAttributes)
		for (j <- 0 until inst.numAttributes) {
			result.setValue(j, inst.value(j))
		}
		for (j <- 0 until labels.size) {
			//set unclassified
			result.setValue(j + inst.numAttributes(), -1.0)
		}
		output.add(result)
	}

	def query(query: Instances): Instances = throw new UnsupportedOperationException

	def configure(properties: Properties) = {}

}

class ResultMergeProcessorFactory extends ProcessorFactory(classOf[ResultMergeProcessor])

object ResultMergeProcessorFactory {
	val INPUT_SEGMENTS = "segments"
	val INPUT_NONSEGMENTS = "nonsegments"
	val INPUT_RAWDATA = "rawdata"
}