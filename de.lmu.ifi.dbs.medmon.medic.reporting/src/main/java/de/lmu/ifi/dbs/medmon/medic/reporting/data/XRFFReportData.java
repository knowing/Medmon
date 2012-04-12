package de.lmu.ifi.dbs.medmon.medic.reporting.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.Loader;
import weka.core.converters.Saver;
import weka.core.converters.XRFFSaver;

public class XRFFReportData extends JAXBReportData {

	private Path	dataPath;

	public XRFFReportData(Path dataPath) {
		this.dataPath = dataPath;
	}

	@Override
	public String getId() {
		return "xrff";
	}

	@Override
	public void marshal(OutputStream outputStream) {

		try (InputStream inputStream = Files.newInputStream(dataPath)) {

			Loader loader = new ArffLoader();
			loader.setSource(inputStream);
			Instances dataset = loader.getDataSet();
			Saver saver = new XRFFSaver();
			saver.setInstances(dataset);

			saver.setDestination(outputStream);
			saver.writeBatch();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
