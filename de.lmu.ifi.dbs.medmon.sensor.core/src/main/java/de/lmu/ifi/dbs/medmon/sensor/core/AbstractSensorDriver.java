package de.lmu.ifi.dbs.medmon.sensor.core;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.device.Constants;
import org.osgi.service.device.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSensorDriver implements ISensorDriver {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    @SuppressWarnings("rawtypes")
    public int match(ServiceReference reference) throws Exception {
        Object category = reference.getProperty(Constants.DEVICE_CATEGORY);
        if (!(category instanceof Category))
            return Device.MATCH_NONE;

        return match((Category) category, reference);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String attach(ServiceReference reference) throws Exception {
        BundleContext ctx = reference.getBundle().getBundleContext();
        ISensor sensor = ctx.getService(reference);
        sensor.setDriver(this);
        log.debug("Attched driver " + getClass().getSimpleName() + " to sensor " + sensor.getName());
        return null;
    }

    public abstract int match(Category category, ServiceReference reference);

}
