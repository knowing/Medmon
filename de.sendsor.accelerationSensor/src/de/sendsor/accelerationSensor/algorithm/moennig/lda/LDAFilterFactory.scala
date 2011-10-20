package de.sendsor.accelerationSensor.algorithm.moennig.lda

import java.util.Properties
import java.io.{ InputStreamReader, LineNumberReader, IOException }
import scala.collection.immutable.Map
import akka.actor.Actor
import akka.event.EventHandler.{ debug, info, warning, error }
import de.lmu.ifi.dbs.knowing.core.factory._
import de.lmu.ifi.dbs.knowing.core.factory.TFactory._
import de.lmu.ifi.dbs.knowing.core.processing.TSerializable
import de.lmu.ifi.dbs.knowing.core.weka.{ WekaFilter, WekaFilterFactory }
import de.lmu.ifi.dbs.knowing.core.weka.WekaFilterFactory._
import LDAFilterFactory._
import weka.core.Instances
import java.io.PrintWriter
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import Jama.Matrix
import scala.collection.mutable.ListBuffer

class LDAFilterFactory extends WekaFilterFactory[LDAFilterWrapper, LDAFilter](classOf[LDAFilterWrapper], classOf[LDAFilter]) {

  //Creates default Properties which are used if properties aren't set
  override def createDefaultProperties: Properties = {
    val returns = new Properties
    returns.setProperty(DEBUG, "false")
    returns.setProperty(DIMREDUCTION, "false")
    returns.setProperty(ATTRIBUTE_NAME_PREFIX, "FV_");
    returns
  }

  //Possible property values. Editor should show these
  override def createPropertyValues: Map[String, Array[_ <: Any]] = {
    Map(DEBUG -> boolean_property)
    Map(DIMREDUCTION -> boolean_property)
  }

  //Property description
  override def createPropertyDescription: Map[String, String] = {
    Map(DEBUG -> "Debug true/false")
    Map(DIMREDUCTION -> "Dimension reduction true/false")
  }

}

object LDAFilterFactory {
  val DIMREDUCTION = "dimensionreduction"
  val OUTDIMENSIONS = "outputdimensions"
  val ATTRIBUTE_NAME_PREFIX = "attributeNamePrefix"

  //Model sections
  val OUTPUT_FORMAT = "output_format"
  val EIGENVECTOR_MATRIX = "eigenvector_matrix"
}

class LDAFilterWrapper extends WekaFilter(new LDAFilter) with TSerializable {

  //LDA must be trained, before queries are being answered
  isBuild = false

  override def start {
    try {
      val in = inputStream()
      in match {
        case None => warning(this, "No LDA training data found!")
        case Some(i) =>
          debug(this, "Trying to open InputStream")
          val reader = new LineNumberReader(new InputStreamReader(i))
          val line = reader.readLine
          if (line == null) {
            reader.close
            return
          }
          val lda = filter.asInstanceOf[LDAFilter]
          //TODO complete this method
          /*
          val dimInfos = reader.readLine.split(";")
          lda.setInDimensions(dimInfos(0).toInt)
          lda.setOutDimensions(dimInfos(1).toInt)
          lda.setDimensionReduction(dimInfos(2).toBoolean)
          
          val eigenValues = reader.readLine.split(";").map(_.toDouble)
          lda.setEigenValues(eigenValues)
          
          reader.readLine.equals(EIGENVECTOR_MATRIX)
          var row = reader.readLine
          val rows = ListBuffer[String]()
          while(!row.equals(OUTPUT_FORMAT)) {
            rows += row
            row = reader.readLine
          }
         
          val output = new Instances(reader)
          lda.setOutput(output)
                    
          isBuild = true
          */
          reader.close
      }
    } catch {
      case e: IOException => warning(this, e.getMessage)
    }
  }

  override def postStop() {
    val out = outputStream
    out match {
      case None => debug(this, "Trained LDA will not be saved")
      case Some(o) =>
        debug(this, "Trying to open OutputStream and save LDA")
        val writer = new PrintWriter(o)
        val lda = filter.asInstanceOf[LDAFilter]
        writer.print(lda.getInDimensions + ";")
        writer.print(lda.getOutDimensions + ";")
        writer.println(lda.isDimensionReduction)
        val eigenValues = lda.getEigenValues.foldLeft("")((x, y) => x + ";" + y).substring(1)
        writer.println(eigenValues)
        writer.println(EIGENVECTOR_MATRIX)
        val symbols = new DecimalFormatSymbols(Locale.US)
        val df = new DecimalFormat("##########.##########", symbols)
        lda.getEigenVectorMatrix.print(writer, df, 0)
        writer.println(OUTPUT_FORMAT)
        writer.println(lda.getOutput)
        writer.flush
        writer.close
    }
  }

  override def build(instances: Instances) {
    akka.event.EventHandler.debug(this, "Train LDA")
    guessAndSetClassLabel(instances)
    val lda = filter.asInstanceOf[LDAFilter]
    val header = new Instances(instances, 0)
    filter.setInputFormat(header)
    lda.train(instances)
    isBuild = true
    processStoredQueries
  }

  override def configure(properties: Properties) = {
    //Configure your classifier here with
    val lda = filter.asInstanceOf[LDAFilter]
    val debug = properties.getProperty(DEBUG)
    val dimreduction = properties.getProperty(DIMREDUCTION)
    val outdimensions = properties.getProperty(OUTDIMENSIONS)
    val attNamePrefix = properties.getProperty(ATTRIBUTE_NAME_PREFIX);
    lda.setDebug(debug.toBoolean)
    lda.setDimensionReduction(dimreduction.toBoolean)
    if (outdimensions != null) {
      lda.setOutDimensions(outdimensions.toInt)
    }
    lda.setAttributeNamePrefix(attNamePrefix)
  }
}