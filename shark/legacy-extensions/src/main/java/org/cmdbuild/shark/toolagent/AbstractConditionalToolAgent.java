package org.cmdbuild.shark.toolagent;

import static java.lang.String.format;

import org.cmdbuild.workflow.api.SharkWorkflowApi;
import org.cmdbuild.workflow.api.WorkflowApi;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.toolagent.ApplicationBusy;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotDefined;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotStarted;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.api.internal.working.CallbackUtilities;
import org.enhydra.shark.toolagent.AbstractToolAgent;

public abstract class AbstractConditionalToolAgent extends AbstractToolAgent {

	private static final String CMDBUILD_API_CLASSNAME_PROPERTY = "org.cmdbuild.workflow.api.classname";

	public static interface ConditionEvaluator {

		boolean evaluate();

	}

	private final ConditionEvaluator conditionEvaluator;
	private WorkflowApi workflowApi;

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

	public WorkflowApi getWorkflowApi() {
		return workflowApi;
	}

	public String getId() {
		return toolInfo.getId();
	}

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		super.configure(cus);
		configureApi(cus);
	}

	private void configureApi(final CallbackUtilities cus) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		final String classname = cus.getProperty(CMDBUILD_API_CLASSNAME_PROPERTY);
		cus.info(null, format("loading api '%s'", classname));
		final Class<? extends SharkWorkflowApi> sharkWorkflowApiClass = Class.forName(classname).asSubclass(
				SharkWorkflowApi.class);
		final SharkWorkflowApi sharkWorkflowApi = sharkWorkflowApiClass.newInstance();
		sharkWorkflowApi.configure(cus);
		workflowApi = sharkWorkflowApi;
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
				innerInvoke();
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

	protected abstract void innerInvoke() throws Exception;

	@SuppressWarnings("unchecked")
	public <T> T getParameterValue(final String name) {
		final Object value = getParameter(name).the_value;
		return (T) value;
	}

	public <T> void setParameterValue(final String name, final T value) {
		getParameter(name).the_value = value;
	}

	private AppParameter getParameter(final String name) {
		for (final AppParameter parameter : parameters) {
			final String formalName = parameter.the_formal_name;
			if (formalName.equals(name)) {
				return parameter;
			}
		}
		throw new IllegalArgumentException(format("missing parameter for name '%s'", name));
	}

}
