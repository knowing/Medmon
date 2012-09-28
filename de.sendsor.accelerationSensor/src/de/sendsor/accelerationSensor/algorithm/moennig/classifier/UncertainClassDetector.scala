package de.sendsor.accelerationSensor.algorithm.moennig.classifier

import de.lmu.ifi.dbs.knowing.core.processing.TFilter
import weka.core.Instances
import java.util.Properties
import de.lmu.ifi.dbs.knowing.core.factory.ProcessorFactory

/**
 * Sets intervals of instances to classMissing if switching
 * rate is too high
 * 
 * <p>Works on instances which contain a timestamp attribute.</p>
 * 
 * @author Nepomuk Seiler
 * @version 0.1
 */
class UncertainClassDetector extends TFilter {
  
  def filter(instances: Instances): Instances = {
    val returns = new Instances(instances)
    
    // 1. Read window (e.g. 30 seconds)
    // 2. Check changing rate (e.g. 3 classes / 10 times)
    // 3. Set specified amount of instances from beginning to unclassified (e.g. 5 seconds)
    // 4. Move window by specified amount (e.g. 5 seconds)
    
    returns
  }

  def configure(properties: Properties) = {
  }
}


class UncertainClassDetectorFactory extends ProcessorFactory(classOf[UncertainClassDetector])