package de.lmu.ifi.dbs.medmon.base.ui.widgets;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.base.ui.Activator;
import de.lmu.ifi.dbs.medmon.base.ui.viewer.SensorTableViewer;

public class SensorSourceWidget extends Composite {

	private ListenerList		listenerList	= new ListenerList();
	private static final Logger	log				= LoggerFactory.getLogger(Activator.PLUGIN_ID);

	private SensorTableViewer	sensorViewer;

	// protected SensorAdapter sensor;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public SensorSourceWidget(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		sensorViewer = new SensorTableViewer(this, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		sensorViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		sensorViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				log.debug("SensorSourceWidget::selectionChanged()");
				/*
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				sensor = (SensorAdapter) selection.getFirstElement();
				firePropertyChanged("sensor", sensor, (selection.getFirstElement()));
				*/
			}
		});

	}

	public String getFile() {
		log.debug("SensorSourceWidget::getFile()");
		return null;
		//return sensor.getDefaultPath();
	}

	public Object getSensor() {
		log.debug("SensorSourceWidget::getSensor()");
		return null;
		//return sensor;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		listenerList.add(listener);
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		listenerList.add(listener);
	}

	private void firePropertyChanged(String property, Object oldValue, Object newValue) {
		for (Object listener : listenerList.getListeners()) {
			IPropertyChangeListener propertyListener = (IPropertyChangeListener) listener;
			PropertyChangeEvent event = new PropertyChangeEvent(this, property, oldValue, newValue);
			propertyListener.propertyChange(event);
		}
	}

}
