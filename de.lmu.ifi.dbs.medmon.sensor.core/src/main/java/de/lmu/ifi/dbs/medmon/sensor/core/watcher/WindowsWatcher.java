package de.lmu.ifi.dbs.medmon.sensor.core.watcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Does not use the NIO.2 WatcherService.
 * 
 * @author Nepomuk Seiler
 *
 */
public class WindowsWatcher extends MassMediaWatcher {

    private final Logger log = LoggerFactory.getLogger(WindowsWatcher.class);
    
    private final List<Path> devices = new ArrayList<>(24);
    
    @Override
    public void run() {
        try {
        	while(true) {
        		for(int i = 'D'; i <= 'Z'; i++) {
        			char letter = (char)i;
        			Path drive = Paths.get(Character.valueOf(letter) + ":");
        			if(Files.exists(drive) && !devices.contains(drive)) {
        				log.debug("Device added on drive " + drive);
        				devices.add(drive);
        				manager.onSensorEvent(drive, createWatchEvent(drive, StandardWatchEventKinds.ENTRY_CREATE));
        			} else if(!Files.exists(drive) && devices.contains(drive)) {
        				log.debug("Device removed on drive " + drive);
        				devices.remove(drive);
        				manager.onSensorEvent(drive, createWatchEvent(drive, StandardWatchEventKinds.ENTRY_DELETE));
        			}
        		}
        		//Check every 500ms
        		Thread.sleep(500);
        	}
        } catch (InterruptedException e) {
            log.info("Shutdown WindowsMassMediaWatcher");
        }

    }
    
    private WatchEvent<Path> createWatchEvent(final Path drive, final WatchEvent.Kind<Path> kind) {
    	return new WatchEvent<Path>() {

			@Override
			public Path context() {
				return drive;
			}

			@Override
			public int count() {
				return 0;
			}

			@Override
			public WatchEvent.Kind<Path> kind() {
				return kind;
			}
    		
    	};
	}

	@Override
    public List<Path> getDevices() {
   		for(int i = 'D'; i <= 'Z'; i++) {
			char letter = (char)i;
			Path drive = Paths.get(Character.valueOf(letter) + ":");
			if(!Files.exists(drive))
				continue;
			
			devices.add(drive);
		}
        return Collections.unmodifiableList(devices);
    }

    @Override
    protected Path massMediaDeviceDirectory() {
        throw new UnsupportedOperationException("Windows has more than one path to watch");
    }

}
