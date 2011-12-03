package de.lmu.ifi.dbs.medmon.medic.ui;

import javax.persistence.EntityManager;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.medic.core.service.IEntityManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;
import de.lmu.ifi.dbs.medmon.medic.core.service.ISensorManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String										PLUGIN_ID	= "de.lmu.ifi.dbs.medmon.medic.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	// private static ServiceTracker<IPatientService, IPatientService>
	// patientTracker;
	private static ServiceTracker<IEntityManagerService, IEntityManagerService> entityServiceTracker;
	private static ServiceTracker<ISensorManagerService, ISensorManagerService> sensorServiceTracker;
	private static ServiceTracker<IPatientService, IPatientService> patientServiceTracker;
	
	private static final Logger log = LoggerFactory.getLogger(Activator.PLUGIN_ID);

	/**
	 * The constructor
	 */
	public Activator() {
	}
	

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
		
		sensorServiceTracker = new ServiceTracker<ISensorManagerService, ISensorManagerService>(context, ISensorManagerService.class, null);
		sensorServiceTracker.open();
		
		patientServiceTracker = new ServiceTracker<IPatientService, IPatientService>(context, IPatientService.class, null);
		patientServiceTracker.open();
		
		entityServiceTracker = new ServiceTracker<IEntityManagerService, IEntityManagerService>(context, IEntityManagerService.class, null);
		entityServiceTracker.open();
		
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

		sensorServiceTracker.close();
		sensorServiceTracker = null;
		
		patientServiceTracker.close();
		patientServiceTracker = null;
		
		entityServiceTracker.close();
		entityServiceTracker = null;

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
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static ISensorManagerService getSensorService() {
		return sensorServiceTracker.getService();
	}

	public static IPatientService getPatientService() {
		return patientServiceTracker.getService();
	}
	
	public static IEntityManagerService getEntityManagerService() {
		return entityServiceTracker.getService();
	}
}
