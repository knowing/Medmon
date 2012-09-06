package de.lmu.ifi.dbs.medmon.sensor.core.watcher;

import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindowsWatcher extends MassMediaWatcher {

    private final Logger log = LoggerFactory.getLogger(WindowsWatcher.class);
    
    @Override
    public void run() {
        log.warn("Not implemented yet");
    }
    
    @Override
    public List<Path> getDevices() {
        log.warn("Not implemented yet");
        return null;
    }

}
