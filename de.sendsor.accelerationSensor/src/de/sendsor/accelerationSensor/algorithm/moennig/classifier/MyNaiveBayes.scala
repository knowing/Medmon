package de.sendsor.accelerationSensor.algorithm.moennig.classifier
import weka.core.Instances
import weka.core.Attribute
import java.util.ArrayList
import weka.core.DenseInstance
import weka.core.Instance
import de.lmu.ifi.dbs.knowing.core.factory.TFactory
import akka.actor.ActorRef
import akka.actor.Actor.actorOf
import weka.classifiers.Classifier
import de.lmu.ifi.dbs.knowing.core.weka.WekaClassifier
import java.util.Properties
import MyNaiveBayesFactory._
import de.lmu.ifi.dbs.knowing.core.weka.WekaClassifierFactory

class MyNaiveBayes extends WekaClassifier(new MyNaiveBayesImpl) {

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