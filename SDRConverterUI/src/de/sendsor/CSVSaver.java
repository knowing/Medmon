package de.sendsor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

public class CSVSaver {

	private File file;
	private Instances instances;
	private String separator = ",";

	private final PropertyChangeSupport support = new PropertyChangeSupport(this);
	private final String m_MissingValue = "?";

	public void writeBatch() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					PrintWriter writer = new PrintWriter(file);
					int numInstances = instances.numInstances();
					int percent = 0;
					support.firePropertyChange("write", 0, 0);
					for (int i = 0; i < numInstances; i++) {
						writer.println(instanceToString((instances.instance(i))));
						int worked = (100 * i) / numInstances;
						// if(worked != percent)
						support.firePropertyChange("write", percent, worked);
						percent = worked;
					}
					support.firePropertyChange("write", percent, 100);
					writer.flush();
					writer.close();
				} catch (FileNotFoundException e) {
					support.firePropertyChange("error", null, e);
					e.printStackTrace();
				}

			}
		}).start();

	}

	/**
	 * turns an instance into a string. takes care of sparse instances as well.
	 * 
	 * @param inst
	 *            the instance to turn into a string
	 * @return the generated string
	 */
	protected String instanceToString(Instance inst) {
		StringBuffer result;
		Instance outInst;
		int i;
		String field;

		result = new StringBuffer();

		if (inst instanceof SparseInstance) {
			outInst = new DenseInstance(inst.weight(), inst.toDoubleArray());
			outInst.setDataset(inst.dataset());
		} else {
			outInst = inst;
		}

		for (i = 0; i < outInst.numAttributes(); i++) {
			if (i > 0)
				result.append(separator);

			if (outInst.isMissing(i))
				field = m_MissingValue;
			else
				field = outInst.toString(i);

			// make sure that custom field separators, like ";" get quoted
			// correctly  as well
			if ((field.indexOf(separator) > -1) && !field.startsWith("'") && !field.endsWith("'"))
				field = "'" + field + "'";
			
			result.append(field.replaceAll("'", ""));
		}

		return result.toString();
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setInstances(Instances instances) {
		this.instances = instances;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

}
