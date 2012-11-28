package de.lmu.ifi.dbs.medmon.medic.core;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.medic.core.preferences.IMedicPreferences;
import de.lmu.ifi.dbs.medmon.services.IEntityManagerService;
import de.lmu.ifi.dbs.medmon.services.IGlobalSelectionService;
import de.lmu.ifi.dbs.medmon.services.IPatientService;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

    // The plug-in ID
    public static final String PLUGIN_ID = "de.lmu.ifi.dbs.medmon.medic.core"; //$NON-NLS-1$

    // The shared instance
    private static BundleContext context;

    private static final Logger log = LoggerFactory.getLogger(PLUGIN_ID);

    private static ServiceTracker<IEntityManagerService, IEntityManagerService> emServiceTracker;
    private static ServiceTracker<IGlobalSelectionService, IGlobalSelectionService> emSelectionService;
    private static ServiceTracker<IPatientService, IPatientService> emPatientService;

    public static BundleContext getBundleContext() {
        return context;
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
        Activator.context = context;

        emServiceTracker = new ServiceTracker<IEntityManagerService, IEntityManagerService>(context, IEntityManagerService.class, null);
        emServiceTracker.open();

        emSelectionService = new ServiceTracker<IGlobalSelectionService, IGlobalSelectionService>(context, IGlobalSelectionService.class,
                null);
        emSelectionService.open();

        emPatientService = new ServiceTracker<IPatientService, IPatientService>(context, IPatientService.class, null);
        emPatientService.open();

        // createApplicationFolders();
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

    private void createApplicationFolders() {
        IEclipsePreferences node = ConfigurationScope.INSTANCE.getNode(IMedicPreferences.MEDMON_NODE);
        Path root = Paths.get(node.get(IMedicPreferences.MEDMON_DIR, System.getProperty("user.home") + "/.medmon"));

        try {
            Files.createDirectory(root);
            Files.createDirectories(root.resolve(node.get(IMedicPreferences.MEDMON_DPU, "dpu")));
        } catch (FileAlreadyExistsException e) {
            log.debug("Medmon folder already exists");
        } catch (IOException e) {
            log.error("IOException in Activator", e);
        }

    }
}
