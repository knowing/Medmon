package de.lmu.ifi.dbs.medmon.base.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;
import de.lmu.ifi.dbs.medmon.medic.core.service.ISensorManagerService;
import de.lmu.ifi.dbs.knowing.core.service.*;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin  {

	// The plug-in ID
	public static final String PLUGIN_ID = "de.lmu.ifi.dbs.medmon.base.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private static ServiceTracker<ISensorManagerService, ISensorManagerService> sensorTracker;
	private static ServiceTracker<IPatientService, IPatientService> patientServiceTracker;
	private static ServiceTracker<IEvaluateService, IEvaluateService> evaluationService;
	private static ServiceTracker<IDPUDirectory, IDPUDirectory> dpuDirectoryTracker;
	
	private ServiceRegistration<IDPUProvider> provider;
	
	public static BundleContext getBundleContext(){
		return Activator.plugin.getBundle().getBundleContext();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		sensorTracker = new ServiceTracker<ISensorManagerService, ISensorManagerService>(context, ISensorManagerService.class, null);
		sensorTracker.open();
		
		patientServiceTracker = new ServiceTracker<IPatientService, IPatientService>(context, IPatientService.class, null);
		patientServiceTracker.open();
		
		evaluationService = new ServiceTracker<IEvaluateService, IEvaluateService>(context, IEvaluateService.class, null);
		evaluationService.open();
		
		dpuDirectoryTracker = new ServiceTracker<IDPUDirectory, IDPUDirectory>(context, IDPUDirectory.class, null);
		dpuDirectoryTracker.open();
		
		provider = context.registerService(IDPUProvider.class, BundleDPUProvider.newInstance(context.getBundle()), null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		sensorTracker.close();
		sensorTracker = null;
		
		patientServiceTracker.close();
		patientServiceTracker = null;
		
		evaluationService.close();
		evaluationService = null;
		
		dpuDirectoryTracker.close();
		dpuDirectoryTracker = null;
		
		provider.unregister();
		provider = null;
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
	
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public static ISensorManagerService getSensorManagerService() {
		return sensorTracker.getService();
	}
	
	public static IPatientService getPatientService() {
		return patientServiceTracker.getService();
	}
	
	public static IEvaluateService getEvaluationService() {
		return evaluationService.getService();
	}
	
	public static IDPUDirectory getDPUDirectory() {
		return dpuDirectoryTracker.getService();
	}

}
