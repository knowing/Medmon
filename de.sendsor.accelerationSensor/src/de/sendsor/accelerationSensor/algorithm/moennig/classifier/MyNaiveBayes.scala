package de.sendsor.accelerationSensor.algorithm.moennig.classifier

import java.io.{InputStream, OutputStream}
import java.util.{ArrayList,Properties}
import akka.actor.ActorRef
import akka.actor.Actor.actorOf
import akka.event.EventHandler.{debug,info, warning, error}
import weka.core.{Attribute,Instances, Instance, DenseInstance}
import weka.classifiers.Classifier
import de.lmu.ifi.dbs.knowing.core.factory.TFactory
import de.lmu.ifi.dbs.knowing.core.weka.{WekaClassifier,WekaClassifierFactory}
import MyNaiveBayesFactory._
import java.io.ObjectOutputStream
import java.io.ObjectInputStream


class MyNaiveBayes extends WekaClassifier(new MyNaiveBayesImpl) {

  override def serialize(out: OutputStream) {
    val oout = new ObjectOutputStream(out)
    oout.writeObject(classifier)
    oout.flush
    oout.close
  }
  
  override def deserialize(in: InputStream) {
    val oin = new ObjectInputStream(in)
    classifier = oin.readObject.asInstanceOf[MyNaiveBayesImpl]
    val c = classifier.asInstanceOf[MyNaiveBayesImpl]
    guessAndCreateClassLabels(c.inputFormat)
    debug(this,"MyNaiveBayes model loaded")
  }
  
  override def configure(properties:Properties) = {
    val bayes = classifier.asInstanceOf[MyNaiveBayesImpl]
    
    val kernel = properties.getProperty(KERNEL_ESTIMATOR, "false")
    val boolKernel = kernel.toBoolean
    bayes.setUseKernelEstimator(boolKernel)
    
    val supervised = properties.getProperty(SUPERVISED_DISCRETIZATION, "false")
    bayes.setUseSupervisedDiscretization(supervised.toBoolean)
    
    val debug = properties.getProperty(DEBUG, "false")
    bayes.setDebug(debug.toBoolean)
  }
}



object MyNaiveBayesFactory {
	val KERNEL_ESTIMATOR = "kernel-estimator"
	val SUPERVISED_DISCRETIZATION = "supervised-discretization"
	val DEBUG = "debug"
	val boolean_property = Array("true", "false")
}

class MyNaiveBayesFactory extends WekaClassifierFactory[MyNaiveBayes, MyNaiveBayesImpl](classOf[MyNaiveBayes], classOf[MyNaiveBayesImpl]) {

  override def createDefaultProperties: Properties = {
    val returns = new Properties
    returns.setProperty(KERNEL_ESTIMATOR, "false")
    returns.setProperty(SUPERVISED_DISCRETIZATION, "false")
    returns.setProperty(DEBUG, "false")
    returns
  }

  override def createPropertyValues: Map[String, Array[_<:Any]] = {
    Map(KERNEL_ESTIMATOR -> boolean_property,
        SUPERVISED_DISCRETIZATION -> boolean_property,
        DEBUG -> boolean_property)
  }

  override def createPropertyDescription: Map[String, String] = {
        Map(KERNEL_ESTIMATOR -> "?",
        SUPERVISED_DISCRETIZATION -> "?",
        DEBUG -> "Debug true/false")
  }
}