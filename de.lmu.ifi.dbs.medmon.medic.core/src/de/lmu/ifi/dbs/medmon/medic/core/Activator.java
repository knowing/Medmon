package de.lmu.ifi.dbs.medmon.medic.core;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.database.entity.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.preferences.IMedicPreferences;
import de.lmu.ifi.dbs.medmon.medic.core.service.IDBModelService;
import de.lmu.ifi.dbs.medmon.medic.core.service.IEntityManagerService;
import de.lmu.ifi.dbs.medmon.medic.core.service.IGlobalSelectionService;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;
import de.lmu.ifi.dbs.medmon.medic.core.util.JPAUtil;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String														PLUGIN_ID	= "de.lmu.ifi.dbs.medmon.medic.core";	//$NON-NLS-1$

	// The shared instance
	private static Activator														plugin;

	private static final Logger														log			= LoggerFactory.getLogger(PLUGIN_ID);

	private static ServiceTracker<IEntityManagerService, IEntityManagerService>		emServiceTracker;
	private static ServiceTracker<IGlobalSelectionService, IGlobalSelectionService>	emSelectionService;
	private static ServiceTracker<IPatientService, IPatientService>					emPatientService;
	private static ServiceTracker<IDBModelService, IDBModelService>					emDBModelService;

	public static BundleContext getBundleContext() {
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

		emSelectionService = new ServiceTracker<IGlobalSelectionService, IGlobalSelectionService>(context, IGlobalSelectionService.class,
				null);
		emSelectionService.open();

		emPatientService = new ServiceTracker<IPatientService, IPatientService>(context, IPatientService.class, null);
		emPatientService.open();

		emDBModelService = new ServiceTracker<IDBModelService, IDBModelService>(context, IDBModelService.class, null);
		emDBModelService.open();

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
		emSelectionService.close();
		emPatientService.close();
		emDBModelService.close();
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

	public static IGlobalSelectionService getSelectionService() {
		return emSelectionService.getService();
	}

	public static IPatientService getPatientService() {
		return emPatientService.getService();
	}

	public static IDBModelService getDBModelService() {
		return emDBModelService.getService();
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
