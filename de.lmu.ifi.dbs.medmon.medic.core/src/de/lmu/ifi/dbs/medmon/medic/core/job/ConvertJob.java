package de.lmu.ifi.dbs.medmon.medic.core.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.lmu.ifi.dbs.medmon.sensor.core.converter.IConverter;

public class ConvertJob extends Job {

	private static final ConvertRule rule = new ConvertRule();
	
	private final IConverter converter;
	private final String family;
	

	public ConvertJob(String name, IConverter converter, String family) {
		super(name);
		this.converter = converter;
		this.family = family;
		setRule(rule);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Converting...", IProgressMonitor.UNKNOWN);

		return Status.OK_STATUS;
	}
	
	@Override
	public boolean belongsTo(Object family) {
		return family.equals(this.family);
	}

}
