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
    val hz = 100
    
    val div = defaultHz / hz
    //Timestamp Attribute
    val timeAttr = instances.attribute(ATTRIBUTE_TIMESTAMP)

    //Table to return
    val returns = new Instances(instances, instances.size / div)
    
    //25Hz =^ 40ms
    /*
    //If you want to average values
    //Map[Valuename (x,y,z) -> weka.core.Attribute]
    val valAttr = findValueAttributesAsMap(instances)
    val values = new Array[Double](returns.numAttributes) */

    //When to add new instance (row)
    var condition = true
    var oldTimestamp:Long = 0
    for (i <- 0 until instances.size) {
      val inst = instances.get(i)

      //Timestamp
      val timestamp = inst.value(timeAttr).toLong

      //If you want to average
/*      for (attr <- valAttr.values) {
        //Get value at specific column at specific row
        val value = inst.value(attr)
        //do something with this x/y/z value
        //Don't forget to add the correct timestamp as a double in the array
      }*/

      if (oldTimestamp+40<timestamp) {
        //If you want to average
        //returns.add(new DenseInstance(1, values))
        oldTimestamp = timestamp;
        returns.add(inst)
      }
    }

    returns
  }

  def query(query: Instance): Instances = throw new UnsupportedOperationException

  def result(result: Instances, query: Instance) = {}

  def configure(properties: Properties) = {}

}

class NormalizeAndroidSensorDataFactory extends ProcessorFactory(classOf[NormalizeAndroidSensorData])