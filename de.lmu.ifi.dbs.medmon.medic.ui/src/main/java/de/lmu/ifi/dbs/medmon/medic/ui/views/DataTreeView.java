package de.lmu.ifi.dbs.medmon.medic.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.medmon.base.ui.viewer.SensorTableViewer;
import de.lmu.ifi.dbs.medmon.medic.ui.Activator;

public class DataTreeView extends ViewPart {

    public static final String ID = "de.lmu.ifi.dbs.medmon.sensor.DataTreeView";
    private static final Logger log = LoggerFactory.getLogger(Activator.PLUGIN_ID);

    private SensorTableViewer sensorViewer;

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout(SWT.HORIZONTAL));

        Composite cSensor = new Composite(parent, SWT.NONE);
        sensorViewer = new SensorTableViewer(cSensor, SWT.NONE);
        cSensor.setLayout(new FillLayout(SWT.HORIZONTAL));
    }

    @Override
    public void setFocus() {
        sensorViewer.getControl().setFocus();
    }

}