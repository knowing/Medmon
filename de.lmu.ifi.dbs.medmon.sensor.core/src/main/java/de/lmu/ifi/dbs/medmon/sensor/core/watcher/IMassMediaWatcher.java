package de.lmu.ifi.dbs.medmon.sensor.core.watcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import de.lmu.ifi.dbs.medmon.sensor.core.ISensorManager;

public interface IMassMediaWatcher extends Runnable {

    public void setSensorManager(ISensorManager manager);
    
    public List<Path> getDevices() throws IOException;
}
