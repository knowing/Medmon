package de.sendsor.accelerationSensor.algorithm.moennig.classifier

import java.util.{ ArrayList, Arrays, Collections, List => JList, Properties, Map => JMap }
import scala.collection.JavaConversions._
import de.lmu.ifi.dbs.knowing.core.processing.TProcessor
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil
import de.lmu.ifi.dbs.knowing.core.events.Results
import de.lmu.ifi.dbs.knowing.core.factory._
import weka.core.{ Attribute, Instance, Instances }
import java.util.Properties
import java.util.HashMap
import java.util.Map.Entry
import de.lmu.ifi.dbs.knowing.core.factory.TFactory
import akka.actor.ActorRef

import ReClassification._

class ReClassification extends TProcessor {

  val windowCount: HashMap[String, Double] = new HashMap[String, Double]();

  def query(query: Instance): Instances = {
    return null;
  }

  def result(result: Instances, query: Instance) = {}

  def configure(properties: Properties) = {

  }

  override def build(input: Instances) {

    val claIndex: Int = ResultsUtil.guessClassIndex(input);
    input.setClassIndex(claIndex);
    val labels = input.classAttribute.enumerateValues.toList;

    var timeIndex: Int = -1;
    for (i <- 0 until input.numAttributes()) {
      if (input.attribute(i).`type` == Attribute.DATE && timeIndex < 0) {
        timeIndex = i;
      }
    }

    val splittedInput: Map[String, Instances] = ResultsUtil.splitInstanceBySource(input, false);

    val halfSize: Int = WINDOW_SIZE / 2;
    for (inst: Instances <- splittedInput.values) {
      inst.sort(timeIndex);
      val oldLabels: Array[String] = new Array[String](inst.numInstances());
      for (i <- 0 until inst.numInstances()) {
        oldLabels(i) = ResultsUtil.highestProbability(inst.get(i))._2;
      }

      for (i <- 0 until inst.numInstances()) {
        windowCount.clear();
        for (j <- 1 to halfSize) {
          if (i - j > 0) {
            updateWindowCount(oldLabels(i - j), halfSize - (j - 1));
          }
          if (i + j < oldLabels.length) {
            updateWindowCount(oldLabels(i + j), halfSize - (j - 1));
          }
        }

        var total: Double = 0.0;
        var max: Double = 0.0;
        var newClass: String = null;

        val iter = windowCount.entrySet().iterator()
        while (iter.hasNext()) {
          val e: Entry[String, Double] = iter.next();
          val k: String = e.getKey();
          val v: Double = e.getValue().doubleValue(); ;

          total += v;
          if (v > max && !NOT_CLASSIFIED.equals(k)) {
            max = v;
            newClass = k;
          }
        }
        //reclassify
        if (max > total / 2.0 || oldLabels(i) == null) {
          //inst.get(0).setClassValue(newClass);
          for (j <- 0 until labels.size) {
            if (labels(j).equals(newClass)) {
              inst.get(0).setValue(inst.attribute("class" + labels.get(j)), 1);
            } else {
              inst.get(0).setValue(inst.attribute("class" + labels.get(j)), 0);
            }
          }
        }
      }

    }

    val result: Instances = ResultsUtil.appendInstances(input.stringFreeStructure, splittedInput.values.toList);
    sendEvent(new Results(result));
  }

  def updateWindowCount(lab: String, value: Double) {
    var label: String = lab;
    if (label == null) {
      label = NOT_CLASSIFIED;
    }
    if (windowCount.containsKey(label)) {
      windowCount.put(label, windowCount.get(label) + value);
    } else {
      windowCount.put(label, value);
    }
  }

}

object ReClassification {
  val NOT_CLASSIFIED: String = "not classified";
  val WINDOW_SIZE: Int = 750;
}

class ReClassificationFactory extends ProcessorFactory(classOf[ReClassification]) {

}
