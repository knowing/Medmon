package de.lmu.ifi.dbs.medmon.medic.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.medic.reporting.service.IReportingService;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensorManager;
import de.lmu.ifi.dbs.medmon.services.IEntityManagerService;
import de.lmu.ifi.dbs.medmon.services.IGlobalSelectionService;
import de.lmu.ifi.dbs.medmon.services.IPatientService;
import de.lmu.ifi.dbs.medmon.services.ITherapyResultService;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String										PLUGIN_ID	= "de.lmu.ifi.dbs.medmon.medic.ui"; //$NON-NLS-1$

	// The shared instance
	public static Activator plugin;

	// private static ServiceTracker<IPatientService, IPatientService>
	// patientTracker;
	private static ServiceTracker<IEntityManagerService, IEntityManagerService> entityServiceTracker;
	private static ServiceTracker<ISensorManager, ISensorManager> sensorManagerServiceTracker;
	private static ServiceTracker<IPatientService, IPatientService> patientServiceTracker;
	private static ServiceTracker<IGlobalSelectionService, IGlobalSelectionService> globalSelectionServiceTracker;
	private static ServiceTracker<ITherapyResultService, ITherapyResultService> therapyResultServiceTracker;
	private static ServiceTracker<IReportingService, IReportingService> reportingServiceTracker;
	
	private static final Logger log = LoggerFactory.getLogger(Activator.PLUGIN_ID);


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		sensorManagerServiceTracker = new ServiceTracker<ISensorManager, ISensorManager>(context, ISensorManager.class, null);
		sensorManagerServiceTracker.open();
		
		patientServiceTracker = new ServiceTracker<IPatientService, IPatientService>(context, IPatientService.class, null);
		patientServiceTracker.open();
		
		entityServiceTracker = new ServiceTracker<IEntityManagerService, IEntityManagerService>(context, IEntityManagerService.class, null);
		entityServiceTracker.open();
		
		globalSelectionServiceTracker = new ServiceTracker<IGlobalSelectionService, IGlobalSelectionService>(context, IGlobalSelectionService.class, null);
		globalSelectionServiceTracker.open();
	
		therapyResultServiceTracker = new ServiceTracker<ITherapyResultService, ITherapyResultService>(context, ITherapyResultService.class, null);
		therapyResultServiceTracker.open();
		
		reportingServiceTracker = new ServiceTracker<IReportingService, IReportingService>(context, IReportingService.class, null);
		reportingServiceTracker.open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;

		sensorManagerServiceTracker.close();
		sensorManagerServiceTracker = null;
		
		patientServiceTracker.close();
		patientServiceTracker = null;
		
		entityServiceTracker.close();
		entityServiceTracker = null;
	
		globalSelectionServiceTracker.close();
		globalSelectionServiceTracker = null;

		therapyResultServiceTracker.close();
		therapyResultServiceTracker = null;
		
		reportingServiceTracker.close();
		reportingServiceTracker = null;
		
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
	public static BundleContext getBundleContext(){
		return Activator.plugin.getBundle().getBundleContext();
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path- the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static ISensorManager getSensorManagerService() {
		return sensorManagerServiceTracker.getService();
	}

	public static IPatientService getPatientService() {
		return patientServiceTracker.getService();
	}
	
	public static IEntityManagerService getEntityManagerService() {
		return entityServiceTracker.getService();
	}
	
	public static IGlobalSelectionService getGlobalSelectionService() {
		return globalSelectionServiceTracker.getService();
	}
	
	public static ITherapyResultService getTherapyResultService() {
		return therapyResultServiceTracker.getService();
	}
	
	public static IReportingService getReportingService(){
		return reportingServiceTracker.getService();
	}
}
