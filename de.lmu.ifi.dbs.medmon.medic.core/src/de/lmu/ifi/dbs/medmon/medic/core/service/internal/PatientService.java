package de.lmu.ifi.dbs.medmon.medic.core.service.internal;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.osgi.service.component.ComponentContext;

import de.lmu.ifi.dbs.knowing.core.graph.xml.DataProcessingUnit;
import de.lmu.ifi.dbs.medmon.database.model.Data;
import de.lmu.ifi.dbs.medmon.database.model.Patient;
import de.lmu.ifi.dbs.medmon.medic.core.sensor.SensorAdapter;
import de.lmu.ifi.dbs.medmon.medic.core.service.IPatientService;

/**
 * PatientService declarative Service.
 * 
 * @author Nepomuk Seiler
 * @version 1.1
 */
public class PatientService implements IPatientService {
		
	private ComponentContext context;
	private HashMap<String, Object> selections;
	
	private PropertyChangeSupport support = new PropertyChangeSupport(this);
	
	@Override
	public boolean setSelection(ISelection selection) {
		if(selection.isEmpty())
			return false;
		if(selection instanceof IStructuredSelection)
			return handleIStructuredSelection((IStructuredSelection) selection);
		return false;
	}
	
	@Override
	public boolean setSelection(Object object, String clazz) {
		StructuredSelection selection = new StructuredSelection(object);
		return handleIStructuredSelection(selection);
	}
	
	/**
	 * Checks the selected elements
	 * @param selection
	 * @return true if selection was successfully set
	 */
	private boolean handleIStructuredSelection(IStructuredSelection selection) {
		Object first = selection.getFirstElement();
		if(first instanceof Patient) {
			Object oldPatient = selections.put(PATIENT, (Patient)first);
			Object oldAnalyzed = selections.put(ANALYZED_DATA, null);
			Object oldSensor = selections.put(SENSOR, null);
			support.firePropertyChange(PATIENT, oldPatient, first);
			support.firePropertyChange(ANALYZED_DATA, oldAnalyzed, null);
			support.firePropertyChange(SENSOR, oldSensor, null);
		} else if(first instanceof SensorAdapter) {
			Object oldSensor = selections.put(SENSOR, (SensorAdapter)first);
			support.firePropertyChange(SENSOR, oldSensor, first);
		} else if(first instanceof DataProcessingUnit) {
			Object old = selections.put(DPU, (DataProcessingUnit)first);
			support.firePropertyChange(DPU, old, first);
		} else {
			return false;
		}
			
		return true;
	}
	
	private Data[] convert(Object[] selection) {
		Data[] data = new Data[selection.length];
		for(int i=0; i<selection.length; i++)
			data[i] = (Data)selection[i];
		return data;
	}
		
	@Override
	public Object getSelection(String clazz) {
		return selections.get(clazz);
	}
	
	@Override
	public void addPropertyChangeListener(String property,PropertyChangeListener listener) {
		if(property.equals(ANALYZED_DATA) ||
				property.equals(PATIENT) ||
				property.equals(SENSOR))
		support.addPropertyChangeListener(property, listener);
		else
			throw new IllegalArgumentException("Value " + property + " is invalid. Choose from IPatientService");
	}
	
	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}
	
    protected void activate(ComponentContext context){
        this.context = context;
        selections = new HashMap<String, Object>();
        System.out.println("PatientServiceComponent activated");

    }

    protected void deactivate(ComponentContext context){
        this.context = null;
        selections = null;
    }    
}
