package de.lmu.ifi.dbs.medmon.medic.core;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.medic.core.preferences.IMedicPreferences;
import de.lmu.ifi.dbs.medmon.medic.core.service.IEntityManagerService;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "de.lmu.ifi.dbs.medmon.medic.core"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private static final Logger log = LoggerFactory.getLogger(PLUGIN_ID);

	private static ServiceTracker<IEntityManagerService, IEntityManagerService> emServiceTracker;

	public static BundleContext getBundleContext(){
		return Activator.plugin.getBundle().getBundleContext();
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
		super.start(context);
		plugin = this;
		emServiceTracker = new ServiceTracker<IEntityManagerService, IEntityManagerService>(context, IEntityManagerService.class, null);
		emServiceTracker.open();
		createApplicationFolders();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		emServiceTracker.close();
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
	
	public static IEntityManagerService getEntityManagerService() {
		return emServiceTracker.getService();
	}

	private void createApplicationFolders() {
		IPreferenceStore store = plugin.getPreferenceStore();

		Path root = Paths.get(store.getString(IMedicPreferences.DIR_MEDMON_ID));
		try {
			Files.createDirectory(root);
			Files.createDirectories(root.resolve(store.getString(IMedicPreferences.DIR_DPU_ID)));
		} catch (FileAlreadyExistsException e) {
			log.debug("Medmon folder already exists");
		} catch (IOException e) {
			log.error("IOException in Activator", e);
		}

	}

}
