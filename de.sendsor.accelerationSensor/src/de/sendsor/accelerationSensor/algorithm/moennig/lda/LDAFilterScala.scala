package de.sendsor.accelerationSensor.algorithm.moennig.lda

import java.util.{ ArrayList, Arrays, Properties }
import weka.core.{ Attribute, Instances, Instance }
import weka.core.Attribute.{ NUMERIC }
import scala.collection.mutable.{ ListBuffer, ArrayBuffer, HashMap }
import scala.collection.JavaConversions._
import de.lmu.ifi.dbs.knowing.core.processing.TFilter
import de.lmu.ifi.dbs.knowing.core.events._
import Jama.{ Matrix, EigenvalueDecomposition }
import LDAFilterScala._
import scala.util.Sorting

class LDAFilterScala extends TFilter {

  private var dimReduction = false
  private var outDimensions = 0
  private var attributeNamePrefix = "" //use only attributes with the given name prefix for the LDA

  private var inDimensions = 0
  private var eigenVectorMatrix: Matrix = _
  private var eigenValues = Array[Double]()

  private var output: Instances = _

  def query(query: Instance): Instances = {
    null
  }

  def result(result: Instances, query: Instance) = {}

  def configure(properties: Properties) = {}

  def determineOutputFormat(inputFormat: Instances): Instances = {
    //determine the amount of numeric input dimensions
    determineInputDimensions(inputFormat)
    output = output match {
      case null =>
        val result = new Instances(inputFormat, 0)
        (dimReduction, outDimensions, inDimensions) match {
          case (dimRed, out, in) if dimRed && out > 0 && in > out =>
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
    val subset = classList.foldLeft(List[List[List[Double]]]()) { (subset, CurrentClass) =>
      val si = inst.foldLeft(List[List[Double]]()) { (siLocal, in) =>
        classList(in.value(in.classAttribute).toInt) match {
          case CurrentClass => {
            for (
              k <- 0 to inst.numAttributes if (k != inst.classIndex && inst.attribute(k).`type` == NUMERIC)
                && (attributeNamePrefix == null || inst.attribute(k).name.startsWith(attributeNamePrefix))
            ) yield in.value(k)
          }.toList :: siLocal
          case _ => siLocal
        }
      }
      si match {
        case Nil => throw new Exception("Empty subset. Would lead to empty covariance matrix!")
        case _ => si.reverse :: subset
      }
    }.reverse

    statusChanged(Progress("Calculate groupMean", 2, 8))
    // calculate group mean
    val groupMean = Array.ofDim[Double](subset.size, inDimensions)
    for (i <- 0 until groupMean.length) {
      for (j <- 0 until groupMean(i).length) {
        groupMean(i)(j) = getGroupMean(j, subset(i))
      }
    }

    statusChanged(Progress("Calculate totalMean", 3, 8))
    // calculate total mean
    val totalMean = new Array[Double](inDimensions)
    for (i <- 0 until totalMean.length) {
      totalMean(i) = getTotalMean(i, inst)
    }

    statusChanged(Progress("Calculate covariance matrices", 2, 8))
    // calculate covariance matrices
    val covariance = Array.ofDim[Double](subset.size, inDimensions, inDimensions)
    for (i <- 0 until subset.size) {
      covariance(i) = getCovarianceMatrix(subset(i), groupMean(i))
    }

    statusChanged(Progress("Check matrices", 4, 8))
    //test for matrix singularity
    for (i <- 0 until covariance.length) {
      val covM = new Matrix(covariance(i))
      val det = covM.det
      if (Math.abs(det) < SINGULARITY_DETECTION_THRESHOLD || det.isNaN) {
        throw new Exception("matrix got singular...");
      }
    }

    statusChanged(Progress("Calculate within-class scatter", 5, 8))
    //calculate the within-class scatter matrix
    val sw = Array.ofDim[Double](inDimensions, inDimensions)
    for (i <- 0 until covariance.length) {
      for (j <- 0 until covariance(i).length) {
        for (k <- 0 until covariance(i)(j).length) {
          sw(j)(k) += covariance(i)(j)(k)
        }
      }
    }

    statusChanged(Progress("Calculate between-class scatter", 6, 8));
    //calculate the between-class scatter matrix
    val sb = Array.ofDim[Double](inDimensions, inDimensions)
    for (i <- 0 until subset.size) {
      for (j <- 0 until inDimensions) {
        for (k <- 0 until inDimensions) {
          //sb[j][k] += subset[i].size() * (groupMean[i][j]-totalMean[j])*(groupMean[i][k]-totalMean[k]);
          sb(j)(k) += inDimensions * (groupMean(i)(j) - totalMean(j)) * (groupMean(i)(k) - totalMean(k))
        }
      }
    }

    val sbm = new Matrix(sb);
    val swm = new Matrix(sw);

    statusChanged(Progress("EigenvalueDecomposition", 7, 8));
    val criterion = (swm.inverse).times(sbm)
    val evd = new EigenvalueDecomposition(criterion)
    eigenVectorMatrix = evd.getV
    eigenValues = evd.getRealEigenvalues

    statusChanged(Progress("Sort eigenvalues", 8, 8))
    sortEigenvalues()
  }

  /**
   * sort the Eigenvalues in descendig order and rearrange the Eigenvektor matrix accordingly
   */
  private def sortEigenvalues() {
    val evArray = Arrays.copyOf(eigenValues, eigenValues.length)
    val evSorted = new Array[Double](evArray.length)

    val evMap = new HashMap[Double, ArrayBuffer[Int]]()
    for (i <- 0 until evArray.length) {
      evMap.put(evArray(i), ListBuffer[Int](i))
    }

    val evList = evMap.keySet.toList.sortWith((d1, d2) => d1 > d2)

    val normFactor = new Array[Double](inDimensions)
    for (i <- 0 until inDimensions) {
      var absMax = 0.0
      for (j <- 0 until inDimensions if (Math.abs(eigenVectorMatrix.get(j, i)) > absMax)) {
        absMax = Math.abs(eigenVectorMatrix.get(j, i))
      }
      normFactor(i) = absMax
    }

    val temp = eigenVectorMatrix.getArrayCopy
    var newIndex = 0;
    for (j <- 0 until evList.size) {
      evMap(evList(j)) foreach { oldIndex =>
        for (k <- 0 until temp.length) {
          temp(k)(newIndex) = eigenVectorMatrix.get(k, oldIndex) / normFactor(oldIndex)
        }
        evSorted(newIndex) = evList(j)
        newIndex += 1
      }
    }
    eigenVectorMatrix = new Matrix(temp)
    eigenValues = evSorted
  }

  /**
   * calculates the mean value for the column at the given index
   * @param column index of the column to calculate the mean for
   * @param data feature vectors to calculate the mean for
   * @return mean value
   */
  private def getGroupMean(column: Int, data: List[List[Double]]): Double = {
    val d = new Array[Double](data.size)
    for (i <- 0 until data.size) {
      d(i) = data(i)(column)
    }
    getMean(d);
  }

  /**
   * calculates the mean value for the attribute at the given index over all instances
   * @param attIndex index of the attribute to calculate the mean for
   * @param inst instances to calculate the mean for
   * @return mean value
   */
  private def getTotalMean(attIndex: Int, inst: Instances): Double = {
    val d = new Array[Double](inst.numInstances)
    for (i <- 0 until inst.numInstances) {
      d(i) = inst(i).value(attIndex)
    }
    getMean(d);
  }

  /**
   * Returns the mean of the given values. On error or empty data returns 0.
   *
   * @param values The values.
   * @return The mean.
   */
  private def getMean(values: Array[Double]): Double = {
    if (values == null || values.length == 0)
      return Double.NaN;

    var mean = 0.0d

    for (index <- 0 until values.length) {
      mean += values(index)
    }
    mean / (values.length.toDouble)
  }

  /**
   * calculates the covariance matrix for the given feature vectors
   * @param fvs the the feature vectors to calculate the covariances for
   * @param means array with mean values for each feature in the feature vector
   * @return covariance matrix
   */
  private def getCovarianceMatrix(fvs: List[List[Double]], means: Array[Double]): Array[Array[Double]] = {
    val dimension = fvs(0).size
    val covariance = Array.ofDim[Double](dimension, dimension)
    for (i <- 0 until dimension) {
      for (j <- i until dimension) {
        var s = 0.0;
        for (k <- 0 until fvs.size) {
          s += (fvs(k)(j) - means(j)) * (fvs(k)(i) - means(i));
        }
        s /= fvs.size
        covariance(i)(j) = s
        covariance(j)(i) = s
      }
    }
    covariance;
  }

}

object LDAFilterScala {
  val DIMENSION_REDUCTION_PER = 0.95
  val SINGULARITY_DETECTION_THRESHOLD = 1.0E-250
}
