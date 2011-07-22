package de.sendsor.accelerationSensor;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import de.lmu.ifi.dbs.knowing.core.factory.TFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.fv.AugmentedFVFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.lda.LDAFilterFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.preprocessing.TruncatedPeakPredictionFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.segmentation.SegmentationFactory;
import de.sendsor.accelerationSensor.converter.SDRLoaderFactory;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "de.sendsor.accelerationSensor"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private ServiceRegistration sdrService;
	private ServiceRegistration ldaFilterFactory;
	private ServiceRegistration augmentedFV;
	private ServiceRegistration segmentation;
	private ServiceRegistration peakPrediction;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		sdrService = context.registerService(TFactory.class.getName(), new SDRLoaderFactory(), null);
		ldaFilterFactory = context.registerService(TFactory.class.getName(), new LDAFilterFactory(), null);
		augmentedFV = context.registerService(TFactory.class.getName(), new AugmentedFVFactory(), null);
		segmentation = context.registerService(TFactory.class.getName(), new SegmentationFactory(), null);
		peakPrediction = context.registerService(TFactory.class.getName(), new TruncatedPeakPredictionFactory(), null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		sdrService.unregister();
		ldaFilterFactory.unregister();
		augmentedFV.unregister();
		segmentation.unregister();
		peakPrediction.unregister();
		plugin = null;
		super.stop(context);
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
