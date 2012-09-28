package de.lmu.ifi.dbs.medmon.sensor.core.watcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import de.lmu.ifi.dbs.medmon.sensor.core.ISensorManager;

/**
 * 
 * @author Nepomuk Seiler
 * @version 1.0
 *
 */
public interface IMassMediaWatcher extends Runnable {

    public void setSensorManager(ISensorManager manager);
    
    /**
     * This methods is called before the thread is started.
     * So this doesn't have to be sychronized
     * 
     * @return
     * @throws IOException
     */
    public List<Path> getDevices() throws IOException;
}
