package de.sendsor.accelerationSensor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        Path path = pathFromSource(source);
        if (!checkPath(path))
            return null;
        sensor = new AccelerationSensor();
        sensor.sourceDirectory = (Path) source;
        sensor.instance = true;
        return sensor;
    }

    private Path pathFromSource(Object source) throws IOException {
        if (source instanceof Path) {
            return (Path) source;
        } else if (source instanceof String) {
            return Paths.get((String) source);
        } else if (source instanceof URI) {
            return Paths.get((URI) source);
        } else if (source instanceof URL) {
            URL url = (URL) source;
            try {
                return Paths.get(url.toURI());
            } catch (URISyntaxException e) {
                throw new IOException("URL could not be converted to URI", e);
            }
        } else if (source instanceof File) {
            return ((File) source).toPath();
        }
        return null;
    }

    private boolean checkPath(Path path) throws IOException {
        if (path == null)
            return false;

        if (Files.isDirectory(path)) {
            // TODO check for sdr file
            try (DirectoryStream<Path> in = Files.newDirectoryStream(path)) {
                for (Path file : in) {
                    if (file.getFileName().toString().endsWith(".sdr")) {
                        return true;
                    }
                }
            }
        }
        return path.getFileName().toString().endsWith(".sdr");
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
