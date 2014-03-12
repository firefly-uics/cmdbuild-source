package org.cmdbuild.data.store.scheduler;

import java.util.Collections;
import java.util.Map;

public class WorkflowSchedulerJob extends SchedulerJob {

	private static final Map<String, String> EMPTY = Collections.emptyMap();

	private String classname;
	private Map<String, String> parameters;

	public WorkflowSchedulerJob() {
		super();
	}

	public WorkflowSchedulerJob(final Long id) {
		super(id);
	}

	@Override
	public void accept(final SchedulerJobVisitor visitor) {
		visitor.visit(this);
	}

	public String getProcessClass() {
		return classname;
	}

	public void setProcessClass(final String classname) {
		this.classname = classname;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(final Map<String, String> parameters) {
		this.parameters = (parameters == null) ? EMPTY : parameters;
	}

}
