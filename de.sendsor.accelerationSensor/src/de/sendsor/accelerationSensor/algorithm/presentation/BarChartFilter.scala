package de.sendsor.accelerationSensor.algorithm.presentation

import java.util.Properties
import akka.event.EventHandler.{debug, info, warning, error}
import weka.core.{ Instance, Instances, DenseInstance }
import de.lmu.ifi.dbs.knowing.core.processing.TFilter
import de.lmu.ifi.dbs.knowing.core.factory.ProcessorFactory
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil.{ ATTRIBUTE_FROM, ATTRIBUTE_TO, ATTRIBUTE_CLASS, ATTRIBUTE_TIMESTAMP }
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

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

      debug(this, "Starting fold")
      //Fold input instances to intervalInst according to their class
      //Timestamps?!?!?!?!?!?!?!? Wrong order during foldleft, or wrong timestamps?
      val start = (intervalInst, "")
      instances.foldLeft(start) {
        case ((intervalInst, currentClass), inst) =>
          val CurrentClass = currentClass
          classAttr.value(inst.value(classAttr).toInt) match {
            //same class, increase timeInterval
            case CurrentClass =>
              val interval = intervalInst.lastInstance
              interval.setValue(toIndex, inst.value(timeAttr))
              (intervalInst, currentClass)
              
            //new class, create new timeInterval
            case clazz =>
              debug(this, "New class found " + clazz)
              intervalInst.add(new DenseInstance(intervalInst.numAttributes))
              val interval = intervalInst.lastInstance
              interval.setValue(toIndex, inst.value(timeAttr))
              interval.setValue(fromIndex, inst.value(timeAttr))
//              interval.setValue(classIndex, inst.value(classAttr))
              interval.setClassValue(clazz)
              (intervalInst, clazz)
          }
      }._1
      //      val splitInst = ResultsUtil.splitInstanceByAttribute(instances, classAttr.name)

  }

  def query(query: Instance): Instances = null

  def result(result: Instances, query: Instance) {}

  def configure(properties: Properties) {}

}

class BarChartFilterFactory extends ProcessorFactory(classOf[BarChartFilter])