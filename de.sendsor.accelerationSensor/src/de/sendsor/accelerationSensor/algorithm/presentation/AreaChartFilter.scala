package de.sendsor.accelerationSensor.algorithm.presentation

import de.lmu.ifi.dbs.knowing.core.processing.TFilter
import java.util.{ ArrayList, Properties }
import weka.core.{ Attribute, Instances, DenseInstance }
import de.lmu.ifi.dbs.knowing.core.factory.ProcessorFactory
import de.lmu.ifi.dbs.knowing.core.results.TimeSeriesResults
import scala.collection.mutable.ListBuffer

/**
 * @author Nepomuk Seiler
 */
class AreaChartFilter extends TFilter {

  def filter(instances: Instances): Instances = {
    val attrTimestamp = instances.attribute("timestamp")
    val attrClasses = new ListBuffer[Attribute]

    for (i <- 0 until instances.numAttributes) {
      val attr = instances.attribute(i)
      if (attr.name.startsWith("class") && attr.name.length > 5)
        attrClasses += attr
    }

    val results = TimeSeriesResults(attrClasses.map(_.name).toList)

    for (i <- 0 until instances.numInstances) {
      val inst = instances.get(i)
      val values = new Array[Double](attrClasses.size + 1)
      values(0) = inst.value(attrTimestamp)
      for (j <- 0 until attrClasses.size) {
        values(j + 1) = inst.value(attrClasses(j))
      }

      results.add(new DenseInstance(1, values))
    }

    results
  }

  def configure(properties: Properties) = {}

}

class AreaChartFilterFactory extends ProcessorFactory(classOf[AreaChartFilter])