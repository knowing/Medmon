package de.sendsor.accelerationSensor.algorithm.moennig.classifier

import java.util.{ ArrayList, Arrays, Collections, List => JList, Properties, Map => JMap }
import scala.collection.JavaConversions._
import java.util.Properties
import akka.event.EventHandler.{ debug, info, warning, error }
import de.lmu.ifi.dbs.knowing.core.factory.ProcessorFactory
import de.lmu.ifi.dbs.knowing.core.processing.TProcessor
import ResultMergeProcessorFactory._
import weka.core.Instances
import weka.core.Instance
import weka.core.Attribute
import weka.core.FastVector
import java.text.SimpleDateFormat
import java.util.Date
import weka.core.DenseInstance
import de.lmu.ifi.dbs.knowing.core.events.Results
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil


class ResultMergeProcessor extends TProcessor{
  
  var segments: Instances = null;
  var nonsegments: Instances = null;
  
  var inputRelIndex: Int = -1; 
  var inputStartTime: Int = -1;
  
  var sdf: SimpleDateFormat = null;
  
  var labels: List[Any] = null; 

   override def build = {    
    case (inst, None) => debug(this, "Default build is called")
    case (inst, Some(INPUT_SEGMENTS)) => addSegments(inst)
    case (inst, Some(INPUT_NONSEGMENTS)) => addNonSegments(inst)
    case (x, y) => warning(this, "Bullshit!")
  }
  
  def build(instances: Instances) = {  
    warning(this, "This should never be called")
  }

  def query(query: Instance): Instances = { null }

  def result(result: Instances, query: Instance): Unit = {  }

  def configure(properties: Properties): Unit = {  }
  
  def addSegments(segs: Instances){
    segments = segs;
    if(segments!=null && nonsegments!=null){
      merge();
    }
  }
  
  def addNonSegments(nonSegs: Instances){
    nonsegments = nonSegs;
    if(segments!=null && nonsegments!=null){
      merge();
    }
  }
  
  def determineFormat(): Instances = {
    for(i <- 0 until segments.numAttributes){
      if(segments.attribute(i).`type` == Attribute.RELATIONAL && inputRelIndex < 0){
    	  inputRelIndex = i;
      }
      if(segments.attribute(i).`type` == Attribute.DATE && inputStartTime < 0){
    	  inputStartTime = i;
    	  sdf = new SimpleDateFormat(segments.attribute(i).getDateFormat());
      }
    }
    
    val attributes:FastVector[Attribute] = new FastVector();
    val relaltionalAttribute: Instances = segments.attribute(inputRelIndex).relation();
    for(i <- 0 until relaltionalAttribute.numAttributes()){
      attributes.addElement(relaltionalAttribute.attribute(i));
    }
    
    labels = segments.classAttribute.enumerateValues.toList;
    labels foreach (l => attributes.add(new Attribute("class" + l)))
    
    val output = new Instances("data",attributes, 0);
    return output
  }
  
  def merge() {
    
    val output = determineFormat();
    
    val splittedSegments: Map[String, Instances] = ResultsUtil.splitInstanceBySource(segments,false);
    val splittedNonSegments: Map[String, Instances] = ResultsUtil.splitInstanceBySource(nonsegments,false);
    
    for(key: String <- splittedSegments.keys){
    	val segs: Instances = splittedSegments.get(key).get;
    	val nonsegs: Instances = splittedNonSegments.get(key).get;
    	segs.sort(inputStartTime);
    	nonsegs.sort(inputStartTime);
	    var si = 0;
	    var ni = 0;
	    while(si < segs.numInstances() || ni < nonsegs.numInstances()){
	      if(si < segs.numInstances() && ni < nonsegs.numInstances()){
	    	  val sDate: Date = sdf.parse(segs.get(si).stringValue(inputStartTime));
	    	  val nDate: Date = sdf.parse(nonsegs.get(ni).stringValue(inputStartTime));
	    	  if(sDate.before(nDate)){
	    	    addInstance(output, segs.get(si));
	    	    si += 1;
	    	  }
	    	  else{
	    	    addInstance(output, nonsegs.get(ni));
	    	    ni += 1;
	    	  }
	      }
	      else if(si < segs.numInstances()){
	    	  addInstance(output, segs.get(si));
	    	  si += 1;
	      }
	      else{
	    	  addInstance(output, nonsegs.get(ni));
	    	  ni += 1;
	      }
	  	}
    }
    
    sendEvent(new Results(output));
    
  }
  
  def addInstance(output: Instances, inst: Instance){
    val relAtt: Instances = inst.relationalValue(inputRelIndex);
    
    for(i <- 0 until relAtt.numInstances()){
      val result: DenseInstance = new DenseInstance(output.numAttributes());
      for(j <- 0 until relAtt.get(i).numAttributes()){
    	  result.setValue(j, relAtt.get(i).value(j));
      }
      for(j <- 0  until labels.size){
        result.setValue(j+ relAtt.numAttributes(), inst.value(inst.dataset().attribute("class"+labels.get(j))))
      }
      output.add(result);
    }
  }
  
}

class ResultMergeProcessorFactory extends ProcessorFactory(classOf[ResultMergeProcessor]) {
  
}

object ResultMergeProcessorFactory {
  val INPUT_SEGMENTS = "segments"
  val INPUT_NONSEGMENTS = "nonsegments"
}