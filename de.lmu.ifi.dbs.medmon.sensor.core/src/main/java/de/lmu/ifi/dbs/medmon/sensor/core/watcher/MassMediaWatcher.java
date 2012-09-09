package de.lmu.ifi.dbs.medmon.sensor.core.watcher;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.sensor.core.ISensorManager;

public abstract class MassMediaWatcher implements IMassMediaWatcher {

    protected ISensorManager manager;
    protected Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void setSensorManager(ISensorManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            massMediaDeviceDirectory().register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);

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
                	try {
                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        manager.onSensorEvent(resolveFullpath(ev), ev);
                	} catch (Exception e) {
                		log.error("Unable to process WatchEvent", e);
                	}

                }
                if (!key.reset()) {
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return mass media device directory (e.g. Unix /media)
     */
    protected abstract Path massMediaDeviceDirectory();

    /**
     * 
     * @param event
     * @return
     */
    protected Path resolveFullpath(WatchEvent<Path> event) {
        return massMediaDeviceDirectory().resolve(event.context());
    }
    
    @Override
    public List<Path> getDevices() throws IOException {
        LinkedList<Path> returns = new LinkedList<>();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(massMediaDeviceDirectory())) {
            for (Path path : dirStream) {
                returns.add(path);
            }
        }
        return returns;
    }
}
