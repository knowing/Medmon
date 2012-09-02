package de.lmu.ifi.dbs.medmon.sensor.core.watcher;

import de.lmu.ifi.dbs.medmon.sensor.core.ISensorManager;

public interface IMassMediaWatcher extends Runnable {

    public void setSensorManager(ISensorManager manager);
}
