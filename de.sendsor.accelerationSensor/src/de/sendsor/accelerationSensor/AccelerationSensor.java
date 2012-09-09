package de.sendsor.accelerationSensor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import de.lmu.ifi.dbs.medmon.sensor.core.AbstractSensor;
import de.lmu.ifi.dbs.medmon.sensor.core.Category;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

/**
 * 
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 2011-11-21
 * 
 */
public class AccelerationSensor extends AbstractSensor {

    private Path sourceDirectory;
    private static final String DATA_FILE = "input.sdr";

    @Override
    public ISensor create(Object source) throws IOException {
        AccelerationSensor sensor = null;
        if(source instanceof Path) {
            sensor = new AccelerationSensor();
            sensor.sourceDirectory = (Path) source;
        }
        return sensor;
    }
    
    @Override
    public InputStream getDataInputStream() throws IOException {
        return Files.newInputStream(sourceDirectory.resolve(DATA_FILE));
    }
    
    @Override
    public String getSerial() {
        return "aa:bb:cc:dd";
    }

    @Override
    public Category getCategory() {
        return Category.AC;
    }
    

    @Override
    public String getName() {
        return "Sendsor Acceleration Sensor";
    }

    @Override
    public String getDescription() {
        return "3D Acceleration Senesor";
    }


}
