package de.lmu.ifi.dbs.medmon.sensor.core.watcher;

import de.lmu.ifi.dbs.medmon.sensor.core.ISensorManager;

public abstract class MassMediaWatcher implements IMassMediaWatcher {

    protected ISensorManager manager;

    @Override
    public void setSensorManager(ISensorManager manager) {
        this.manager = manager;
        
    }
}
