package de.lmu.ifi.dbs.medmon.medic.core.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.lmu.ifi.dbs.knowing.core.graph.xml.DataProcessingUnit;

public class DataProcessJob extends Job {

	private static final DataProcessRule rule = new DataProcessRule();
	private final DataProcessingUnit dpu;

	public DataProcessJob(String name, DataProcessingUnit dpu) {
		super(name);
		this.dpu = dpu;
		setRule(rule);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		return Status.OK_STATUS;
	}
		

}
