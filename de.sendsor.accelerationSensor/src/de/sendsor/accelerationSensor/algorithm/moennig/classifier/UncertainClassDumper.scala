package de.sendsor.accelerationSensor.algorithm.moennig.classifier

import de.lmu.ifi.dbs.knowing.core.processing.TFilter
import de.lmu.ifi.dbs.knowing.core.factory.ProcessorFactory
import java.util.Properties
import weka.core.{ Attribute, Instances}
import scala.collection.mutable.ListBuffer
import java.util.ArrayList

class UncertainClassDumper extends TFilter {

  import UncertainClassDumper._

  private var threshold = THRESHOLD_DEFAULT

  def filter(instances: Instances): Instances = {
    guessAndSetClassLabel(instances)
    // Find attributes
    val attrTimestamp = instances.attribute("timestamp")
    val attrClasses = new ListBuffer[Attribute]

    for (i <- 0 until instances.numAttributes) {
      val attr = instances.attribute(i)
      if (attr.name.startsWith("class") && attr.name.length > 5)
        attrClasses += attr
    }
    
    val returns = new Instances(instances)
    returns.setClassIndex(-1)
    returns.deleteAttributeAt(guessClassLabel(instances))
    
    returns.insertAttributeAt(new Attribute("classUnclassified"), returns.numAttributes)
    
    val classes = new ArrayList[String](instances.numClasses)
    val enumClasses = instances.classAttribute.enumerateValues
    while(enumClasses.hasMoreElements){
      classes.add(enumClasses.nextElement.toString)
    }
    classes.add("Unclassified")
    val classAttribute = new Attribute("class", classes)
    returns.insertAttributeAt(classAttribute, returns.numAttributes)
    returns.setClassIndex(returns.numAttributes - 1)
    
    // Dump instances
    for (i <- 0 until instances.numInstances) {
      val inst = returns.get(i)
      inst.setValue(returns.attribute("classUnclassified"), 0);
      for (j <- 0 until attrClasses.size) {
        //Very dangerous implementation. threshold must be >0.50
        if(inst.value(attrClasses(j)) > threshold) {
          inst.setClassValue("Unclassified")
          inst.setValue(returns.attribute("classUnclassified"), 1);
        }
      }
    }

    returns
  }

  def configure(properties: Properties) = {
    threshold = properties.getProperty(THRESHOLD, THRESHOLD_DEFAULT.toString).toDouble
  }

}

object UncertainClassDumper {

  val THRESHOLD = "threshold"
  val THRESHOLD_DEFAULT = 0.80
}

class UncertainClassDumperFactory extends ProcessorFactory(classOf[UncertainClassDumper])