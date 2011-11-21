package de.sendsor.accelerationSensor.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import de.lmu.ifi.dbs.knowing.core.util.OSGIUtil;
import de.lmu.ifi.dbs.knowing.core.service.*;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;
import de.sendsor.accelerationSensor.AccelerationSensor;
import de.sendsor.accelerationSensor.SDRLoaderFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.classifier.ReClassificationFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.classifier.ResultMergeProcessorFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.classifier.MyNaiveBayesFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.fv.AugmentedFVFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.lda.LDAFilterFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.preprocessing.SourceToClassConverterFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.preprocessing.TruncatedPeakPredictionFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.segmentation.SegmentationFactory;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "de.sendsor.accelerationSensor"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private OSGIUtil util;
	private ServiceRegistration<IDPUProvider> dpuService;

	private ServiceRegistration<ISensor> sensorService;
	

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		plugin = this;
		
		sensorService = context.registerService(ISensor.class, new AccelerationSensor(), null);
		
		util = new OSGIUtil(context);
		util.registerLoader(new SDRLoaderFactory());
		util.registerProcessor(new LDAFilterFactory());
		util.registerProcessor(new AugmentedFVFactory());
		util.registerProcessor(new SegmentationFactory());
		util.registerProcessor(new MyNaiveBayesFactory());
		util.registerProcessor(new TruncatedPeakPredictionFactory());
		util.registerProcessor(new SourceToClassConverterFactory());
		util.registerProcessor(new ResultMergeProcessorFactory());
		util.registerProcessor(new ReClassificationFactory());
		dpuService = context.registerService(IDPUProvider.class, BundleDPUProvider.newInstance(context.getBundle()), null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		dpuService.unregister();
		util.deregisterAll();
		sensorService.unregister();
		sensorService = null;
		util = null;
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
