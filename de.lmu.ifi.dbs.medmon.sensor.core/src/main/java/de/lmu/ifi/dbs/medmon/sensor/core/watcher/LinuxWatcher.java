package de.lmu.ifi.dbs.medmon.sensor.core.watcher;

import java.nio.file.Path;
import java.nio.file.Paths;

public class LinuxWatcher extends MassMediaWatcher {

    private static final Path media = Paths.get("/", "media");

    @Override
    protected Path massMediaDeviceDirectory() {
        return media;
    }

}
