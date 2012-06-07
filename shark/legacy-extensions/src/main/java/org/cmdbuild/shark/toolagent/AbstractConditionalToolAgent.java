package org.cmdbuild.shark.toolagent;

import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.toolagent.ApplicationBusy;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotDefined;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotStarted;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.toolagent.AbstractToolAgent;

public abstract class AbstractConditionalToolAgent extends AbstractToolAgent {

	public static interface ConditionEvaluator {

		boolean evaluate();

	}

	private final ConditionEvaluator conditionEvaluator;

	public AbstractConditionalToolAgent() {
		conditionEvaluator = null;
	}

	public AbstractConditionalToolAgent(final ConditionEvaluator conditionEvaluator) {
		this.conditionEvaluator = conditionEvaluator;
	}

	WMSessionHandle getSessionHandle() {
		return shandle;
	}

	WMEntity getToolInfo() {
		return toolInfo;
	}

	String getProcessInstanceId() {
		return procInstId;
	}

	public String getAssId() {
		return assId;
	}

	@Override
	public void invokeApplication(final WMSessionHandle shandle, final long handle, final WMEntity appInfo,
			final WMEntity toolInfo, final String applicationName, final String procInstId, final String assId,
			final AppParameter[] parameters, final Integer appMode) throws ApplicationNotStarted,
			ApplicationNotDefined, ApplicationBusy, ToolAgentGeneralException {
		super.invokeApplication(shandle, handle, appInfo, toolInfo, applicationName, procInstId, assId, parameters,
				appMode);
		setStatus(APP_STATUS_RUNNING);
		try {
			if (conditionEvaluator().evaluate()) {
				innerInvoke(shandle, handle, appInfo, toolInfo, applicationName, procInstId, assId, parameters, appMode);
			}
			setStatus(APP_STATUS_FINISHED);
		} catch (final Exception e) {
			setStatus(APP_STATUS_INVALID);
			throw new ToolAgentGeneralException(e);
		}
	}

	private ConditionEvaluator conditionEvaluator() {
		return (conditionEvaluator != null) ? conditionEvaluator : new SharkConditionalEvaluator(this);
	}

	protected void setStatus(final long status) {
		this.status = status;
	}

	protected abstract void innerInvoke(WMSessionHandle shandle, long handle, WMEntity appInfo, WMEntity toolInfo,
			String applicationName, String procInstId, String assId, AppParameter[] parameters, Integer appMode)
			throws ApplicationNotStarted, ApplicationNotDefined, ApplicationBusy, ToolAgentGeneralException;

}
