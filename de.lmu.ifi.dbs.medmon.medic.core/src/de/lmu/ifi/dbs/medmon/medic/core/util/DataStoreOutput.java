package de.lmu.ifi.dbs.medmon.medic.core.util;

import java.io.OutputStream;

import org.codehaus.aspectwerkz.transform.AspectWerkzPreProcessor.Output;

import de.lmu.ifi.dbs.medmon.database.model.Data;

public class DataStoreOutput implements AutoCloseable {

	public OutputStream	outputStream;
	public Data			dataEntity;

	public DataStoreOutput(OutputStream outputStream, Data data) {
		this.outputStream = outputStream;
		this.dataEntity = data;
	}
	
	@Override
	public void close(){
		
	}
}
