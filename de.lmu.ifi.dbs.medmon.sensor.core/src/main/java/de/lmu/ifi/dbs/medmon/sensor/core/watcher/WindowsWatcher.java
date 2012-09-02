package de.lmu.ifi.dbs.medmon.sensor.core.watcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.sensor.core.ISensorManager;

public class WindowsWatcher implements IMassMediaWatcher {

    private final Logger log = LoggerFactory.getLogger(WindowsWatcher.class);
    private ISensorManager manager;
    
    @Override
    public void run() {
        log.warn("Not implemented yet");
    }

    @Override
    public void setSensorManager(ISensorManager manager) {
        this.manager = manager;
    }

}
