package de.sendsor.accelerationSensor.algorithm.presentation

import java.util.Properties
import akka.event.EventHandler.{ debug, info, warning, error }
import weka.core.{ Instance, Instances, DenseInstance }
import de.lmu.ifi.dbs.knowing.core.processing.TFilter
import de.lmu.ifi.dbs.knowing.core.factory.ProcessorFactory
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil.{ ATTRIBUTE_FROM, ATTRIBUTE_TO, ATTRIBUTE_CLASS, ATTRIBUTE_TIMESTAMP }
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

/**
 * <p>Filters the input Instances and returns a ResultsUtil.timeIntervalResult.<br>
 * Input instances must have:
 * <li>ResultsUtil.ATTRIBUTE_TIMESTAMP</li>
 * <li>ClassAttribute or ResultsUtil.ATTRIBUTE_CLASS</li>
 * </p>
 * 
 * @author Nepomuk Seiler
 * @version 1.0
 * @since 2011-11-17
 */
class BarChartFilter extends TFilter {

  override def filter(instances: Instances): Instances = guessAndSetClassLabel(instances) match {
    case -1 => instances
    case i =>
      val classAttr = instances.classAttribute
      val timeAttr = instances.attribute(ATTRIBUTE_TIMESTAMP)
      val classList = classAttr.enumerateValues.toList.asInstanceOf[List[String]]

      //Create timeIntervalResult Instances and get attribute indices
      val intervalInst = ResultsUtil.timeIntervalResult(classList)
      val fromIndex = intervalInst.attribute(ATTRIBUTE_FROM).index
      val toIndex = intervalInst.attribute(ATTRIBUTE_TO).index
      val classIndex = intervalInst.attribute(ATTRIBUTE_CLASS).index

      debug(this, "Create TimeIntervalInstances")
      var currentClass = ""
      for (i <- 0 until instances.numInstances) {
        val inst = instances(i)
        classAttr.value(inst.value(classAttr).toInt) match {

          //alter the end date of the interval
          case c if c.equals(currentClass) =>
            val interval = intervalInst.lastInstance
            interval.setValue(toIndex, inst.value(timeAttr))

          //Create a new interval
          case c =>
            intervalInst.add(new DenseInstance(intervalInst.numAttributes))
            val interval = intervalInst.lastInstance
            interval.setValue(toIndex, inst.value(timeAttr))
            interval.setValue(fromIndex, inst.value(timeAttr))
            interval.setClassValue(c)
            currentClass = c
        }
      }

      intervalInst
  }

  def query(query: Instance): Instances = null

  def result(result: Instances, query: Instance) {}

  def configure(properties: Properties) {}

}

class BarChartFilterFactory extends ProcessorFactory(classOf[BarChartFilter])