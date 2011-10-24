package de.lmu.ifi.dbs.medmon.medic.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionListener;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionProvider;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;
import de.lmu.ifi.dbs.medmon.medic.core.service.ISensorService;
import de.lmu.ifi.dbs.medmon.medic.ui.selection.PatientSelectionProvider;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String										PLUGIN_ID	= "de.lmu.ifi.dbs.medmon.medic.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator										plugin;
	private static BundleContext									context;

	private static ServiceTracker<IPatientService, IPatientService>	patientTracker;
	private static ServiceTracker<ISensorService, ISensorService>	sensorTracker;
	private static PatientSelectionProvider							patientSelectionProvider;

	public static PatientSelectionProvider getPatientSelectionProvider() {
		return patientSelectionProvider;
	}

	public static void registerSelectionListener(IGlobalSelectionListener<?> service) {
		context.registerService(IGlobalSelectionListener.class, service, null);
		System.out.println("************************************REGISTER");
	}

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
		System.out.println("#########################################START");
		super.start(context);
		plugin = this;
		this.context = context;

		patientSelectionProvider = new PatientSelectionProvider();
		context.registerService(IGlobalSelectionProvider.class, patientSelectionProvider, null);

		patientTracker = new ServiceTracker<IPatientService, IPatientService>(context, IPatientService.class.getName(), null);
		patientTracker.open();

		sensorTracker = new ServiceTracker<ISensorService, ISensorService>(context, ISensorService.class, null);
		sensorTracker.open();
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
		patientTracker.close();
		patientTracker = null;

		sensorTracker.close();
		sensorTracker = null;

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

	public static IPatientService getPatientService() {
		return patientTracker.getService();
	}

	public static ISensorService getSensorService() {
		return sensorTracker.getService();
	}
}
