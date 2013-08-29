package org.cmdbuild.services.scheduler;

import java.util.Collections;
import java.util.Map;

import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.scheduler.AbstractJob;
import org.cmdbuild.workflow.CMWorkflowException;

public class StartProcessJob extends AbstractJob {

	private static final boolean ALWAYS_ADVANCE = true;

	private final WorkflowLogic workflowLogic;

	public StartProcessJob(final String name, final WorkflowLogic workflowLogic) {
		super(name);
		this.workflowLogic = workflowLogic;
	}

	@Override
	public void execute() {
		if (isValidJob()) {
			final String processClassName = detail;
			final Map<String, String> processVars = params;
			Log.WORKFLOW.info(String.format("Starting scheduled process %s", processClassName));
			for (final String key : params.keySet()) {
				Log.WORKFLOW.info(String.format("  %s -> %s", key, params.get(key)));
			}
			try {
				workflowLogic.startProcess(processClassName, processVars, Collections.<String, Object> emptyMap(),
						ALWAYS_ADVANCE);
			} catch (final CMWorkflowException e) {
				Log.WORKFLOW.info("Cannot start scheduled process", e);
			}
		} else {
			Log.WORKFLOW.info("Invalid process");
		}
	}

	private boolean isValidJob() {
		return ((detail != null) && (params != null));
	}
}
