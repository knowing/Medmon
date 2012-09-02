package de.lmu.ifi.dbs.medmon.sensor.core.internal;

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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches for changes in the common mount directories for removable devices
 * like USB, SD cards and external harddisks.
 * 
 * 
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 2012-05-21
 */
public class DeviceService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private Map<String, Object> properties;
    private Watcher watcher;

    public void activate(Map<String, Object> properties) {
        this.properties = properties;
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("linux")) {
                watchLinux();
            } else if (os.contains("win")) {
                watchWindows();
            }
        } catch (IOException e) {
            log.error("Failed to start device service!", e);
            throw new RuntimeException("Failed to start device service!", e);
        }

    }

    protected void watchLinux() throws IOException {
        // /dev/disk/by-path/ ---> dev/sd abcdef
        // /media/
        Path media = Paths.get("/", "media");
        if (!Files.exists(media)) {
            log.error("No mount folder 'media' found!");
            return;
        }
        watcher = new Watcher(media);
        watcher.start();
    }

    protected void watchWindows() {
        Iterable<Path> roots = FileSystems.getDefault().getRootDirectories();
        // TODO start Watcher for each root
        // TODO Watch on each drive letter (c-z), what if letter not exists?
    }

    public void deactivate() {
        watcher.interrupt();
        watcher = null;
    }

    /**
     * React on changes in the configuration
     * 
     * @param properties
     */
    public void modified(Map<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * Thread to watch particular directory
     * 
     * @author Nepomuk Seiler
     * 
     */
    private class Watcher extends Thread {

        private final WatchService watchService;
        private final Path root;

        public Watcher(Path root) throws IOException {
            this.root = root;
            initialSearch();
            watchService = FileSystems.getDefault().newWatchService();
            root.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
        }

        private void initialSearch() {
            try (DirectoryStream<Path> dir = Files.newDirectoryStream(root)) {
                for (final Path device : dir) {
                    fireEvent(new WatchEvent<Path>() {
                        @Override
                        public java.nio.file.WatchEvent.Kind<Path> kind() {
                            return StandardWatchEventKinds.ENTRY_CREATE;
                        }

                        @Override
                        public int count() {
                            return 0;
                        }

                        @Override
                        public Path context() {
                            return device;
                        }
                    });
                }
            } catch (IOException e) {
                log.error("Failed to search media directory: " + root, e);
            }
        }

        @Override
        public void run() {
            log.info("Watching device path " + root);
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
                    fireEvent((WatchEvent<Path>) event);
                }
                if (!key.reset()) {
                    return;
                }
            }
        }

        private void fireEvent(WatchEvent<Path> event) {
            Path filename = event.context();
            log.debug("Device changed " + filename);
            if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
            } else if (event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
            }
        }
    }

}
