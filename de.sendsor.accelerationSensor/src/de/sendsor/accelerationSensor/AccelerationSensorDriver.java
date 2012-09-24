package de.sendsor.accelerationSensor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.joda.time.Interval;
import org.osgi.framework.ServiceReference;

import weka.core.Instances;
import de.lmu.ifi.dbs.knowing.core.results.TimeSeriesResults;
import de.lmu.ifi.dbs.medmon.sensor.core.AbstractSensorDriver;
import de.lmu.ifi.dbs.medmon.sensor.core.Category;

public class AccelerationSensorDriver extends AbstractSensorDriver {

    private static final Instances header = TimeSeriesResults.newInstances(Arrays.asList(new String[] { "x", "y", "z" }));

    @Override
    public Instances getData(InputStream in) throws IOException {
        SDRConverter converter = new SDRConverter(in);
        return converter.getData();
    }

    @Override
    public Interval getInterval(InputStream in) throws IOException {
        SDRConverter converter = new SDRConverter(in);
        return converter.getInterval();
    }

    @Override
    public Instances getHeader() {
        return header;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public int match(Category category, ServiceReference reference) {
        if (!category.equals(Category.AC))
            return 0;
        Object sensor = reference.getBundle().getBundleContext().getService(reference);
        if (sensor instanceof AccelerationSensor)
            return 1;
        return 0;
    }
}
