package org.cmdbuild.services.scheduler.job;

import java.util.Collections;
import java.util.Map;

import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.WorkflowLogic;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.CMWorkflowException;

public class StartProcessJob extends AbstractJob {

	private static final boolean ALWAYS_ADVANCE = true;

	public StartProcessJob(int id) {
		super(id);
	}

	@Override
	public void execute() {
		if (isValidJob()) {
			final String processClassName = detail;
			final Map<String, String> processVars = params;
			Log.WORKFLOW.info(String.format("Starting scheduled process %s", processClassName));
			for (String key : params.keySet()) {
				Log.WORKFLOW.info(String.format("  %s -> %s", key, params.get(key)));
			}
			final UserContext systemCtx = UserContext.systemContext();
			final WorkflowLogic workflowLogic = TemporaryObjectsBeforeSpringDI.getWorkflowLogic(systemCtx);
			try {
				workflowLogic.startProcess(processClassName, processVars, Collections.<String, Object>emptyMap(), ALWAYS_ADVANCE);
			} catch (CMWorkflowException e) {
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
