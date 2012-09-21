package de.sendsor.accelerationSensor.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.device.Constants;
import org.osgi.service.device.Device;
import org.osgi.service.device.Driver;

import de.lmu.ifi.dbs.knowing.core.util.OSGIUtil;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensorDriver;
import de.sendsor.accelerationSensor.AccelerationSensor;
import de.sendsor.accelerationSensor.AccelerationSensorDriver;
import de.sendsor.accelerationSensor.SDRLoaderFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.classifier.MyNaiveBayesFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.classifier.ReClassificationFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.classifier.ResultMergeProcessorFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.classifier.UncertainClassDumperFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.fv.AugmentedFVFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.lda.LDAFilterFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.preprocessing.SourceToClassConverterFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.preprocessing.TruncatedPeakPredictionFactory;
import de.sendsor.accelerationSensor.algorithm.moennig.segmentation.SegmentationFactory;
import de.sendsor.accelerationSensor.algorithm.presentation.ACDataSaverFactory;
import de.sendsor.accelerationSensor.algorithm.presentation.AreaChartFilterFactory;
import de.sendsor.accelerationSensor.algorithm.presentation.BarChartFilterFactory;
import de.sendsor.accelerationSensor.algorithm.walonka.NormalizeAndroidSensorDataFactory;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

    // The plug-in ID
    public static final String PLUGIN_ID = "de.sendsor.accelerationSensor"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    private OSGIUtil util;

    private ServiceRegistration<?> sensorService;
    private ServiceRegistration<?> driverService;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        plugin = this;

        // TODO Create Util methods for this
        String[] driverClasses = new String[] { ISensorDriver.class.getName(), Driver.class.getName() };
        String[] sensorClasses = new String[] { ISensor.class.getName(), Device.class.getName() };

        AccelerationSensor sensor = new AccelerationSensor();
        Dictionary<String, Object> sensorProps = new Hashtable<>();
        sensorProps.put(Constants.DEVICE_CATEGORY, sensor.getCategory());
        sensorProps.put(Constants.DEVICE_DESCRIPTION, sensor.getDescription());

        driverService = context.registerService(driverClasses, new AccelerationSensorDriver(), null);
        sensorService = context.registerService(sensorClasses, sensor, sensorProps);

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
        util.registerProcessor(new UncertainClassDumperFactory());
        util.registerProcessor(new BarChartFilterFactory());
        util.registerProcessor(new AreaChartFilterFactory());
        util.registerProcessor(new NormalizeAndroidSensorDataFactory());
        util.registerSaver(new ACDataSaverFactory());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        util.deregisterAll();
        sensorService.unregister();
        sensorService = null;
        driverService.unregister();
        driverService = null;
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
