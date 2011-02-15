package org.cmdbuild.shark.toolagent;

import java.util.HashMap;
import java.util.Map;

import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.client.wfservice.XPDLBrowser;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.toolagent.ApplicationBusy;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotDefined;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotStarted;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.toolagent.AbstractToolAgent;
import org.enhydra.shark.utilities.WMEntityUtilities;

import bsh.Interpreter;

/**
 * Abstract ToolAgent class which support "conditional" execution, if the
 * ExtendedAttribute "Condition" evaluate to true, then the abstract method
 * innerInvoke is called, otherwise the toolagent finish without doing anything.
 */
public abstract class AbstractConditionalToolAgent extends AbstractToolAgent {

	String condition = null;

	String cmdbuildProcessClass;
	int cmdbuildProcessId;

	@SuppressWarnings("unchecked")
	protected <T> T get(final AppParameter[] params, final int idx) {
		return (T) params[idx].the_value;
	}

	protected int getInt(final AppParameter[] params, final int idx) {
		return ((Long) params[idx].the_value).intValue();
	}

	protected void loadCmdbuildAttributes() throws Exception {
		final WAPI wapi = Shark.getInstance().getWAPIConnection();

		// first get the process variables
		this.cmdbuildProcessClass = (String) wapi.getProcessInstanceAttributeValue(shandle, procInstId, "ProcessClass")
				.getValue();
		final Long tmp = (Long) wapi.getProcessInstanceAttributeValue(shandle, procInstId, "ProcessId").getValue();
		this.cmdbuildProcessId = tmp.intValue();
	}

	public boolean hasCondition() {
		return condition != null;
	}

	@Override
	public void invokeApplication(final WMSessionHandle shandle, final long handle, final WMEntity appInfo,
			final WMEntity toolInfo, final String applicationName, final String procInstId, final String assId,
			final AppParameter[] parameters, final Integer appMode) throws ApplicationNotStarted,
			ApplicationNotDefined, ApplicationBusy, ToolAgentGeneralException {

		super.invokeApplication(shandle, handle, appInfo, toolInfo, applicationName, procInstId, assId, parameters,
				appMode);

		this.status = APP_STATUS_RUNNING;

		try {
			loadCmdbuildAttributes();
			configureCondition();

			if (eval()) {
				innerInvoke(shandle, handle, appInfo, toolInfo, applicationName, procInstId, assId, parameters, appMode);
			} else {
				this.cus.debug(shandle, "condition for " + appName + " valuated to false.");
			}

			status = APP_STATUS_FINISHED;
		} catch (final Exception e) {
			e.printStackTrace();
			status = APP_STATUS_INVALID;
			throw new ToolAgentGeneralException(e);
		}
	}

	protected abstract void innerInvoke(WMSessionHandle shandle, long handle, WMEntity appInfo, WMEntity toolInfo,
			String applicationName, String procInstId, String assId, AppParameter[] parameters, Integer appMode)
			throws ApplicationNotStarted, ApplicationNotDefined, ApplicationBusy, ToolAgentGeneralException;

	private boolean eval() {

		final String info = this.toolInfo.getActId() + "(tool #" + this.toolInfo.getOrdNo() + " - "
				+ this.toolInfo.getId() + ")";
		if (hasCondition()) {
			// obtain the context and call the interpreter
			try {
				final Map<String, Object> ctxt = obtainContext();
				final boolean conditionValue = evaluate(ctxt);
				System.err.println("Condition " + condition + " in  " + info + " evaluated to " + conditionValue);
				return conditionValue;
			} catch (final Exception e) {
				System.err.println("Exception evaluating condition for " + info);
				e.printStackTrace();
				return false;
			}
		} else {
			System.err.println("No condition for " + info);
			return true;
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> obtainContext() throws Exception {
		final Map<String, Object> out = new HashMap();
		final WAPI wapi = Shark.getInstance().getWAPIConnection();

		// first get the process variables
		for (final WMAttribute attr : wapi.listProcessInstanceAttributes(shandle, procInstId, null, false).getArray()) {
			out.put(attr.getName(), attr.getValue());
		}

		// from the ToolAgent docs the assId parameter is the WorkItemId
		for (final WMAttribute attr : wapi.listWorkItemAttributes(shandle, procInstId, assId, null, false).getArray()) {
			// overwrite process variables, as the workItem ones are likely to
			// be updated.
			out.put(attr.getName(), attr.getValue());
		}
		return out;
	}

	private boolean evaluate(final Map<String, Object> context) throws Exception {
		final Interpreter intr = new Interpreter();
		for (final String key : context.keySet()) {
			intr.set(key, context.get(key));
		}
		final Object res = intr.eval(condition);
		if (res instanceof Boolean) {
			return (Boolean) res;
		}
		return false;
	}

	private void configureCondition() {
		condition = null;
		try {
			final XPDLBrowser xpdlb = Shark.getInstance().getXPDLBrowser();
			condition = WMEntityUtilities.findEAAndGetValue(shandle, xpdlb, toolInfo, "Condition");
		} catch (final Exception e) {
			// Skipping condition
		}
	}

	protected AppParameter getParameter(final AppParameter[] params, final String name) {
		for (final AppParameter p : params) {
			if (p.the_formal_name.equals(name))
				return p;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected <T> T getParameterValue(final AppParameter[] params, final String name) {
		return (T) getParameter(params, name).the_value;
	}
}
