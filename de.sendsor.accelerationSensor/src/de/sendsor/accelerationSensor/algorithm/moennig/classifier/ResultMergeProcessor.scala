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
  var rawdata: Instances = null;
  
  var inputRelIndex: Int = -1; 
  var inputStartTime: Int = -1;
  
  var sdf: SimpleDateFormat = null;
  var sdfRaw: SimpleDateFormat = null;
  
  var labels: List[Any] = null; 

   override def build = {    
    case (inst, None) => debug(this, "Default build is called")
    case (inst, Some(INPUT_RAWDATA)) => addRawdata(inst)
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
    if(segments!=null && nonsegments!=null && rawdata!=null){
      merge();
    }
  }
  
  def addNonSegments(nonSegs: Instances){
    nonsegments = nonSegs;
    if(segments!=null && nonsegments!=null && rawdata!=null){
      merge();
    }
  }
  
  def addRawdata(rawd: Instances){
    rawdata = rawd;
    if(segments!=null && nonsegments!=null && rawdata!=null){
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
    
    val splittedRawdata: Map[String, Instances] = ResultsUtil.splitInstanceBySource(rawdata,false);
    val splittedSegments: Map[String, Instances] = ResultsUtil.splitInstanceBySource(segments,false);
    val splittedNonSegments: Map[String, Instances] = ResultsUtil.splitInstanceBySource(nonsegments,false);
    
    for(key: String <- splittedRawdata.keys){    	
    	val raw: Instances = splittedRawdata.get(key).get;
    	val segs: Instances = splittedSegments.get(key).get;
    	val nonsegs: Instances = splittedNonSegments.get(key).get;
    	segs.sort(inputStartTime);
    	nonsegs.sort(inputStartTime);
	    var si = 0;
	    var ni = 0;
	    var ri = 0;
	    
	    sdfRaw = new SimpleDateFormat(raw.attribute(inputStartTime).getDateFormat());
	    
	    while(si < segs.numInstances() || ni < nonsegs.numInstances()){
	      if(si < segs.numInstances() && ni < nonsegs.numInstances()){
	    	  val sDate: Date = sdf.parse(segs.get(si).stringValue(inputStartTime));
	    	  val nDate: Date = sdf.parse(nonsegs.get(ni).stringValue(inputStartTime));
	    	  var rDate: Date = sdfRaw.parse(raw.get(ri).stringValue(inputStartTime));
	    	  while(rDate.before(sDate) && rDate.before(nDate)){
	    	    addRawInstnace(output, raw.get(ri));
	    	    ri += 1;
	    	    rDate = sdfRaw.parse(raw.get(ri).stringValue(inputStartTime));
	    	  }
	    	  if(sDate.before(nDate)){
	    	    ri += addRelationalInstance(output, segs.get(si));
	    	    si += 1;
	    	  }
	    	  else{
	    	    ri += addRelationalInstance(output, nonsegs.get(ni));
	    	    ni += 1;
	    	  }
	      }
	      else if(si < segs.numInstances()){
	    	  val sDate: Date = sdf.parse(segs.get(si).stringValue(inputStartTime));	    	  
	    	  var rDate: Date = sdfRaw.parse(raw.get(ri).stringValue(inputStartTime));
	    	  while(rDate.before(sDate)){
	    	    addRawInstnace(output, raw.get(ri));
	    	    ri += 1;
	    	    rDate = sdfRaw.parse(raw.get(ri).stringValue(inputStartTime));
	    	  }
	    	  ri += addRelationalInstance(output, segs.get(si));
	    	  si += 1;
	      }
	      else{	    	  
	    	  val nDate: Date = sdf.parse(nonsegs.get(ni).stringValue(inputStartTime));
	    	  var rDate: Date = sdfRaw.parse(raw.get(ri).stringValue(inputStartTime));
	    	  while(rDate.before(nDate)){
	    	    addRawInstnace(output, raw.get(ri));
	    	    ri += 1;
	    	    rDate = sdfRaw.parse(raw.get(ri).stringValue(inputStartTime));
	    	  }
	    	  ri += addRelationalInstance(output, nonsegs.get(ni));
	    	  ni += 1;
	      }
	  	}
		while(ri < raw.numInstances()){
		    addRawInstnace(output, raw.get(ri));
		    ri += 1;		    
		  }
    }
    
    sendEvent(new Results(output));
    
  }
  
  def addRelationalInstance(output: Instances, inst: Instance):Int = {
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
    return relAtt.numInstances();
  }
  
  def addRawInstnace(output: Instances, inst: Instance){
     val result: DenseInstance = new DenseInstance(output.numAttributes());
      for(j <- 0 until inst.numAttributes()){
    	  result.setValue(j, inst.value(j));
      }
      for(j <- 0  until labels.size){
        //set unclassified
        result.setValue(j+ inst.numAttributes(), -1.0)
      }
      output.add(result);
  }
  
}

class ResultMergeProcessorFactory extends ProcessorFactory(classOf[ResultMergeProcessor]) {
  
}

object ResultMergeProcessorFactory {
  val INPUT_SEGMENTS = "segments"
  val INPUT_NONSEGMENTS = "nonsegments"
  val INPUT_RAWDATA = "rawdata"
}