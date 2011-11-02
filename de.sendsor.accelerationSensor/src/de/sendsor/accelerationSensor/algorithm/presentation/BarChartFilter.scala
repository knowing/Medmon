package de.sendsor.accelerationSensor.algorithm.presentation

import java.util.Properties
import weka.core.{ Instance, Instances, DenseInstance }
import de.lmu.ifi.dbs.knowing.core.processing.TFilter
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil.{ ATTRIBUTE_FROM, ATTRIBUTE_TO }
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

class BarChartFilter extends TFilter {

  override def filter(instances: Instances): Instances = guessAndSetClassLabel(instances) match {
    case -1 => instances
    case i =>
      val classAttr = instances.classAttribute
      val classList = classAttr.enumerateValues.toList.asInstanceOf[List[String]]

      val start = (ListBuffer[Instances](), classList(0))

      val intervalInst = ResultsUtil.timeIntervalResult(classList)
      val fromIndex = intervalInst.attribute(ATTRIBUTE_FROM).index
      val toIndex = intervalInst.attribute(ATTRIBUTE_TO).index

      val ret = instances.foldLeft(start) { (instList, inst) =>
        classAttr.value(inst.value(classAttr).toInt) match {
          //same class, increase timeInterval
          case instList._2 =>
            val interval = instList._1.head.get(0)
            interval.setValue(fromIndex, inst.value(fromIndex))
            instList
          //new class, create new timeInterval
          case clazz =>
            instList._1 + ResultsUtil.timeIntervalResult(classList)
            val instances = instList._1.head
            val interval = new DenseInstance(instances.numAttributes)
            instances.add(interval)
            interval.setValue(toIndex, inst.value(toIndex))
            interval.setValue(fromIndex, inst.value(fromIndex))
            (instList._1, clazz)
        }
      }
      //      val splitInst = ResultsUtil.splitInstanceByAttribute(instances, classAttr.name)

      instances
  }

  def query(query: Instance): Instances = null

  def result(result: Instances, query: Instance) {}

  def configure(properties: Properties) {}

}