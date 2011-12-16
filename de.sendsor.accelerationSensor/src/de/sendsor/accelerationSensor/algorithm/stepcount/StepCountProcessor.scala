package de.sendsor.accelerationSensor.algorithm.stepcount

import java.util.Properties
import weka.core.{ Instance, Instances }
import de.lmu.ifi.dbs.knowing.core.processing.TProcessor
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil.{timeSeriesResult,findValueAttributesAsMap,ATTRIBUTE_TIMESTAMP}

class StepCountProcessor extends TProcessor {

  //Parameter for algorithm
  var thresholdtip = 0.25f // Minima has to be below this value
  var windowLength = 150 // Number of Values in one Window
  var steplengthMin = 661 // Minimum length of one step in milliseconds
  var steplengthMax = 1700 // Maximum length of one step in milliseconds
  var lastVal = 0.0 //Starting value
  var steps = 0 // Starting value for number of steps
  var intensityLevel = 0.1 // Minimum intesity in windows so steps are calculated

  var data = List[List[Double]]() // Vector contains all Data
 var window = List[List[Double]]() // Simulating one window of the Sensor

  //Parameter Kalman filter
  var errorCovariance = 1
  var sensorError = 59
  var processNoise = 12
  var dt = 1.47

  //private val kalmanFilter = new KalmanFilter
  
  //Output format: step, timestamp
  
  //Input format: x,y,z,timestamp
  lazy val header = timeSeriesResult(List("x", "y", "z"))
  lazy val timeAttribute = header.attribute(ATTRIBUTE_TIMESTAMP)
  lazy val valueAttributes = findValueAttributesAsMap(header)

  def build(instances: Instances) = {}

  private def transform(instances: Instances): Instances = {
    val returns = new Instances(header)
	for(i <- 0 until instances.size) {
	  val inst = instances.get(i)
	  val kalmanFilter = new KalmanFilter(errorCovariance)
	  // Adding x and z axis together
            val x = ((inst.value(valueAttributes("x"))/128F)*2)+0.981 //0.981 Gravity
            val z = ((inst.value(valueAttributes("x"))/128F)*2)
            var xplusz = Math.pow(x,2)+Math.pow(z,2)
            xplusz = Math.round(xplusz*1000)/1000.
            
            //filtering with Kalman Filter
            kalmanFilter.timeUpdate(dt, processNoise)
            kalmanFilter.measurementUpdate2(xplusz, sensorError)
            xplusz = kalmanFilter.getExpectedValue()
	}
    
    returns 
  }

  def configure(properties: Properties) = {}

  def query(query: Instance): Instances = { null }

  def result(result: Instances, query: Instance) = {}

}