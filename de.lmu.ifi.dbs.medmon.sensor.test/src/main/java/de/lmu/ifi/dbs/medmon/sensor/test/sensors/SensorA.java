package de.lmu.ifi.dbs.medmon.sensor.test.sensors;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import weka.core.Instances;

import de.lmu.ifi.dbs.medmon.sensor.core.AbstractSensor;
import de.lmu.ifi.dbs.medmon.sensor.core.Category;
import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;

public class SensorA extends AbstractSensor {

    private Path sourceFolder;

    public SensorA() {
    }

    @Override
    public String getName() {
        return "SensorA";
    }

    @Override
    public String getDescription() {
        return "Test Sensor A";
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
    public Instances getData() throws IOException {
        if (getDriver() == null)
            throw new IOException("No driver found!");

        Instances header = getDriver().getHeader();
        sourceFolder.resolve("datafile.dat");
        return header;
    }

    @Override
    public InputStream getDataInputStream() throws IOException {
        return Files.newInputStream(sourceFolder);
    }

    @Override
    public ISensor create(Object source) throws IOException {
        Path sourceFolder = null;
        if (source instanceof Path) {
            sourceFolder = (Path) source;
        } else if (source instanceof URI) {
            source = Paths.get((URI) source);
        }
        if (sourceFolder == null)
            return null;

        SensorA sensor = new SensorA();
        sensor.sourceFolder = sourceFolder;

        return sensor;
    }

}
