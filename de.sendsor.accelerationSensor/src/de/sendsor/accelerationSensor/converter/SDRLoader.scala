package de.sendsor.accelerationSensor.converter

import akka.actor.ActorRef
import akka.actor.Actors.actorOf

import scala.collection.immutable.Map
import java.util.Properties
import java.io.File

import de.lmu.ifi.dbs.knowing.core.factory.TFactory
import de.lmu.ifi.dbs.knowing.core.processing.TLoader
import de.lmu.ifi.dbs.knowing.core.processing.TLoader._

import weka.core.Instances


/**
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 08.05.2011
 *
 */
class SDRLoader extends TLoader {

  val converter = new SDRConverter
  
  def getDataSet(): Instances = converter.getDataSet

  def configure(properties: Properties) = { 
   val path = properties.getProperty(FILE)
   converter.setFile(new File(path));
  }

  def reset = converter.reset
}

class SDRLoaderFactory extends TFactory {

  val name: String = SDRLoaderFactory.name
  val id: String = SDRLoaderFactory.id

  def getInstance(): ActorRef = actorOf(classOf[SDRLoader])

  def createDefaultProperties(): Properties = {
    val props = new Properties
    props.setProperty(FILE, "")
    props.setProperty(URL, "")
    props
  }

  def createPropertyValues(): Map[String, Array[Any]] = Map()

  def createPropertyDescription(): Map[String, String] = Map()

}

object SDRLoaderFactory {
  val name: String = "SDR Loader"
  val id: String = classOf[SDRLoader].getName
}