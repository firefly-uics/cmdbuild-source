package org.cmdbuild.services.scheduler;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.scheduler.AbstractJob;
import org.cmdbuild.workflow.CMWorkflowException;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class StartProcessJob extends AbstractJob {

	private static Logger logger = Log.WORKFLOW;
	private static Marker marker = MarkerFactory.getMarker(StartProcessJob.class.getName());

	private static final Map<String, Object> NO_WIDGETS = Collections.emptyMap();
	private static final boolean ALWAYS_ADVANCE = true;

	private final WorkflowLogic workflowLogic;

	protected String processClassName;
	protected Map<String, String> processVars;

	public StartProcessJob(final String name, final WorkflowLogic workflowLogic) {
		super(name);
		this.workflowLogic = workflowLogic;
	}

	public void setDetail(final String detail) {
		this.processClassName = detail;
	}

	public void setParams(final Map<String, String> params) {
		this.processVars = params;
	}

	@Override
	public void execute() {
		if (isValidJob()) {
			logger.info(marker, "starting scheduled process '{}'", processClassName);
			for (final Entry<String, String> entry : processVars.entrySet()) {
				logger.info(marker, "\t'{}' -> '{}'", entry.getKey(), entry.getValue());
			}
			try {
				workflowLogic.startProcess(processClassName, processVars, NO_WIDGETS, ALWAYS_ADVANCE);
			} catch (final CMWorkflowException e) {
				logger.info(marker, "error starting scheduled process", e);
			}
		} else {
			logger.info(marker, "invalid process");
		}
	}

	private boolean isValidJob() {
		return ((processClassName != null) && (processVars != null));
	}

}
