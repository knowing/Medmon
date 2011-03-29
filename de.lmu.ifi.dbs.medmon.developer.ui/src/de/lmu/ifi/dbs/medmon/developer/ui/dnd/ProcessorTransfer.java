package de.lmu.ifi.dbs.medmon.developer.ui.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import de.lmu.ifi.dbs.medmon.datamining.core.parameter.IProcessorParameter;
import de.lmu.ifi.dbs.medmon.datamining.core.parameter.NumericParameter;
import de.lmu.ifi.dbs.medmon.datamining.core.parameter.XMLParameterWrapper;
import de.lmu.ifi.dbs.medmon.datamining.core.processing.XMLDataProcessor;

/**
 * DataProcessor serialization format is as follows:
 * (String)name of DataProcessor
 * (String)id of DataProcessor
 * (String)providedBy - The Bundle name which distribute this DataProcessor
 * @author Nepomuk Seiler
 *
 */
public class ProcessorTransfer extends ByteArrayTransfer {

	private static ProcessorTransfer instance = new ProcessorTransfer();
	
	private static final String TYPE_NAME = "Processor-Transfer-Format";
	private static final int TYPE_ID = registerType(TYPE_NAME);
	
	public static ProcessorTransfer getInstance() {
		return instance;
	}
	
	@Override
	protected void javaToNative(Object object, TransferData transferData) {
		byte[] bytes = toByteArray((XMLDataProcessor[])object);
		if(bytes != null)
			super.javaToNative(bytes, transferData);
	}
	
	@Override
	protected Object nativeToJava(TransferData transferData) {
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		return fromByteArray(bytes);
	}
	
	protected XMLDataProcessor[] fromByteArray(byte[] bytes) {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
		
		try {
			//Read array size
			int n = in.readInt();
			//Read processors
			XMLDataProcessor[] returns = new XMLDataProcessor[n];
			for (int i = 0; i < n; i++) {
				XMLDataProcessor processor = readProcessor(in);
				if(processor == null)
					return null;
				returns[i] = processor;
			}
			return returns;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private XMLDataProcessor readProcessor(DataInputStream in) throws IOException  {
		String name = in.readUTF();
		String id = in.readUTF();
		String provider = in.readUTF();
		int count = in.readInt();
		ArrayList<XMLParameterWrapper> parameters = new ArrayList<XMLParameterWrapper>(count+5);
		for(int i=0; i < count; i++) {
			String key = in.readUTF();
			String value = in.readUTF();
			String type = in.readUTF();
			XMLParameterWrapper xmlParameter = new XMLParameterWrapper(key, value, type);
			if(type.equals(IProcessorParameter.INT_TYPE)) {
				int min = in.readInt();
				int max = in.readInt();
				xmlParameter.setMin(String.valueOf(min));
				xmlParameter.setMax(String.valueOf(max));
			}
			parameters.add(xmlParameter);
		}
		return new XMLDataProcessor(name, id, provider, parameters.toArray(new XMLParameterWrapper[parameters.size()]));
	}
	
	protected byte[] toByteArray(XMLDataProcessor[] processors) {
	      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	      DataOutputStream out = new DataOutputStream(byteOut);

	      byte[] bytes = null;

	      try {
	         /* write number of markers */
	         out.writeInt(processors.length);

	         /* write markers */
	         for (int i = 0; i < processors.length; i++) {
	            writeProcessor((XMLDataProcessor)processors[i], out);
	         }
	         out.close();
	         bytes = byteOut.toByteArray();
	      } catch (IOException e) {
	         //when in doubt send nothing
	      }
	      return bytes;
	}
	
	/**
	 * UTFString: name
	 * UTFString: ID
	 * UTFString: providedBy
	 * Integer:	  parameters count
	 * for(parameters count)
	 *  UTFWrite: key
	 *  UTFWrite: value
	 *  UTFWrite: type
	 * @param processor
	 * @param dataOut
	 * @throws IOException
	 */
	private void writeProcessor(XMLDataProcessor processor, DataOutputStream dataOut) throws IOException {
		dataOut.writeUTF(processor.getName());
		dataOut.writeUTF(processor.getId());
		dataOut.writeUTF(processor.getProvidedby());
		
		Map<String, IProcessorParameter> parameters = processor.getParameters();
		//MapSize
		dataOut.writeInt(parameters.size());
		for (String key : parameters.keySet()) {
			dataOut.writeUTF(key);
			dataOut.writeUTF(String.valueOf(parameters.get(key).getValue()));
			dataOut.writeUTF(parameters.get(key).getType());
			IProcessorParameter p = parameters.get(key);
			if(p instanceof NumericParameter) {
				NumericParameter parameter = (NumericParameter) p;
				dataOut.writeInt(parameter.getMinimum());
				dataOut.writeInt(parameter.getMaximum());
			}
		}
	}
	
	@Override
	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}

	@Override
	protected int[] getTypeIds() {
		return new int[] { TYPE_ID };
	}

}
