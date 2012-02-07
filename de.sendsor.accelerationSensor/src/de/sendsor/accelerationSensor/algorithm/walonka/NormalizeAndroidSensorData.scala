package de.sendsor.accelerationSensor.algorithm.walonka

import java.util.Properties
import weka.core.{ Instances, Attribute, DenseInstance, Instance }
import de.lmu.ifi.dbs.knowing.core.factory.ProcessorFactory
import de.lmu.ifi.dbs.knowing.core.processing.TFilter
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil.findValueAttributesAsMap
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil.ATTRIBUTE_TIMESTAMP

class NormalizeAndroidSensorData extends TFilter {

  /**
   * Just prints out the map and returns the head of the map or emptyResult
   */
  override def filter(instances: Instances): Instances = {
    //static field
    val defaultHz = 25
    
    val filtered = new Instances(instances, instances.size)

    //run it with scala paralell collection
    val grouped = instances.par groupBy {
      inst =>
        val timestamp = inst.value(0).toLong
        val millis = timestamp % 1000
        val times = millis / 40 //40ms == 25hz
        val floor = times * 40
        val ceil = (times + 1) * 40

        val FloorVal = millis - floor
        val CeilVal = ceil - millis
        val nearest = Math.min(FloorVal, CeilVal)
        nearest match {
          case FloorVal => timestamp - FloorVal
          case CeilVal => timestamp + CeilVal
        }
    }
    //TODO -> check that no gaps in timestamps

    //average values -> extract in custom method
    grouped.par foreach {
      case (timestamp, b) if b.isEmpty => throw new Exception("Empty timeslot")

      //only one value
      case (timestamp, b) if b.size == 1 =>
        val inst = new DenseInstance(b.head)
        inst.setValue(0, timestamp)
        filtered.add(inst)

      //average | choose 
      case (timestamp, b) =>
        val inst = new DenseInstance(b.head)
        inst.setValue(0, timestamp)
        val sum = b.foldLeft(0.0)((sum, inst) => sum + inst.value(1))
        val avg = sum / b.size
        inst.setValue(1, avg)
        filtered.add(inst)
    }
    filtered.sort(0)
    filtered
  }

  def query(query: Instance): Instances = throw new UnsupportedOperationException

  def result(result: Instances, query: Instance) = {}

  def configure(properties: Properties) = {}

}

class NormalizeAndroidSensorDataFactory extends ProcessorFactory(classOf[NormalizeAndroidSensorData])
