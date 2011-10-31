package de.sendsor.accelerationSensor.algorithm.moennig.lda

import java.util.ArrayList
import scala.collection.JavaConversions._
import weka.core.{ Attribute, Instances }
import weka.core.Attribute.{ NUMERIC }
import scala.collection.mutable.ListBuffer

class LDAFilterScala {

  private var dimReduction = 1
  private var outDimensions = 1
  private var inDimensions = 2

  private var attributeNamePrefix = ""

  private var output: Instances = _

  def determineOutputFormat(inputFormat: Instances): Instances = {
    //determine the amount of numeric input dimensions
    determineInputDimensions(inputFormat)
    output = output match {
      case null =>
        val result = new Instances(inputFormat, 0)
        (dimReduction, outDimensions, inDimensions) match {
          case (dimRed, out, in) if dimRed > 0 && out > 0 && in > out =>
            val range = (in - out, inputFormat.numInstances) match {
              case (toDelete, numInst) if toDelete > numInst => numInst - 1 to 0 by -1
              case (toDelete, numInst) if toDelete <= numInst => numInst - 1 to toDelete by -1
            }
            for (i <- range if (attributeNamePrefix == null || inputFormat.attribute(i).name.startsWith(attributeNamePrefix)))
              result.deleteAttributeAt(i)
            for (i <- 0 until result.numAttributes if (attributeNamePrefix != null && result.attribute(i).name.startsWith(attributeNamePrefix)))
              result.renameAttribute(i, attributeNamePrefix + (i + 1))
            result
          case _ => throw new IllegalArgumentException("output dimensions have to be lower than input dimensions (" + outDimensions + " !< " + inDimensions + ")")

        }
      case out => out
    }
    output
  }

  def determineInputDimensions(inputFormat: Instances) {

  }

  def train(inst: Instances) {
    //This cast is save -> capabilities allow only String or Norminal classes
    val classList = inst.enumerateAttributes.toList.asInstanceOf[List[String]]
    // divide data into subsets
    val subset = classList.foldLeft(List[List[List[Double]]]()) { (subset,CurrentClass) =>
      val si = inst.foldLeft(List[List[Double]]()) { (siLocal, in) =>
        classList(in.value(in.classAttribute).toInt) match {
          case CurrentClass => {
            for (k <- 0 to inst.numAttributes if (k != inst.classIndex && inst.attribute(k).`type` == NUMERIC)
                && (attributeNamePrefix == null || inst.attribute(k).name.startsWith(attributeNamePrefix))
            ) yield in.value(k)}.toList :: siLocal
          case _ => siLocal
        }
      }
      si match {
        case Nil => throw new Exception("Empty subset. Would lead to empty covariance matrix!")
        case _ => si.reverse :: subset
      }
    }.reverse
    
    //		for (int i = 0; i < classList.size(); i++) {
    //			String currentClasss = classList.get(i);
    //			val si = new ArrayList<ArrayList<Double>>();			
    //			for (int j = 0; j < inst.numInstances(); j++) {		
    //				Instance in = inst.get(j);
    //				String cla = inst.classAttribute().value((int)(in.value(inst.classAttribute())));
    //				if(cla.equals(currentClasss)){
    //					val al = new ArrayList<Double>();
    //					for(int k=0;k<in.numAttributes();k++){
    //						if(k!=inst.classIndex() && inst.attribute(k).type() == Attribute.NUMERIC){
    //							if(attributeNamePrefix == null || inst.attribute(k).name().startsWith(attributeNamePrefix)){
    //								al.add(in.value(k));
    //							}
    //						}
    //					}
    //					si.add(al);
    //				}
    //			}
    //			if(si.isEmpty())
    //				throw new Exception("Empty subset. Would lead to empty covariance matrix!");
    //			
    //			subset.add(i, si);
    //		}
  }
}
