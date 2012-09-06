package de.lmu.ifi.dbs.medmon.sensor.core.watcher;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinuxWatcher extends MassMediaWatcher {
    
    private final Logger log = LoggerFactory.getLogger(LinuxWatcher.class);
    private final Path media = Paths.get("/", "media");

    @Override
    public void run() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
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
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    manager.onSensorEvent(media.resolve(ev.context()), ev);
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
    public List<Path> getDevices() throws IOException {
        LinkedList<Path> returns = new LinkedList<>();
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(media)) {
            for (Path path : dirStream) {
                returns.add(path);
            }
        }
        return returns;
    }

}
