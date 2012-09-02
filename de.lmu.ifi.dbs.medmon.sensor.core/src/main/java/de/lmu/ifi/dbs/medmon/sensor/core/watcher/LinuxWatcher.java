package de.lmu.ifi.dbs.medmon.sensor.core.watcher;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.sensor.core.ISensorManager;

public class LinuxWatcher implements IMassMediaWatcher {
    
    private final Logger log = LoggerFactory.getLogger(LinuxWatcher.class);
    private ISensorManager manager;

    @Override
    public void run() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path media = Paths.get("/", "media");
            media.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
            while (true) {
                // wait for key to be signaled
                WatchKey key;
                try {
                    log.debug("waiting for event");
                    key = watchService.take();
                } catch (InterruptedException e) {
                    log.warn("Device Watcher got interrupted.");
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    log.debug("Something happend " + kind);
                    //TODO implement onSensorEvent
                    manager.onSensorEvent();
                }
                if (!key.reset()) {
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setSensorManager(ISensorManager manager) {
        this.manager = manager;
    }

}
