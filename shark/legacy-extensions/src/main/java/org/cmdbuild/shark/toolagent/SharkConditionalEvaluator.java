package org.cmdbuild.shark.toolagent;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.shark.toolagent.AbstractConditionalToolAgent.ConditionEvaluator;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.client.wfservice.XPDLBrowser;
import org.enhydra.shark.utilities.WMEntityUtilities;

import bsh.Interpreter;

@Legacy("It is the same ugly code wrote a long time ago in a Galaxy far, far away... and never changed!")
public class SharkConditionalEvaluator implements ConditionEvaluator {

	public static final String CONDITION_EXTENDED_ATTRIBUTE = "Condition";

	private final AbstractConditionalToolAgent toolAgent;

	private String condition = null;

	public SharkConditionalEvaluator(final AbstractConditionalToolAgent toolAgent) {
		this.toolAgent = toolAgent;
	}

	@Override
	public boolean evaluate() {
		configureCondition();
		return eval();
	}

	private void configureCondition() {
		condition = null;
		try {
			final XPDLBrowser xpdlb = Shark.getInstance().getXPDLBrowser();
			condition = WMEntityUtilities.findEAAndGetValue(toolAgent.getSessionHandle(), xpdlb,
					toolAgent.getToolInfo(), CONDITION_EXTENDED_ATTRIBUTE);
		} catch (final Exception e) {
			// Skipping condition
		}
	}

	private boolean eval() {
		final WMEntity toolInfo = toolAgent.getToolInfo();
		final String info = toolInfo.getActId() + "(tool #" + toolInfo.getOrdNo() + " - " + toolInfo.getId() + ")";
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

	private boolean hasCondition() {
		return condition != null;
	}

	private Map<String, Object> obtainContext() throws Exception {
		final Map<String, Object> out = new HashMap<String, Object>();
		final WAPI wapi = Shark.getInstance().getWAPIConnection();

		// first get the process variables
		for (final WMAttribute attr : wapi.listProcessInstanceAttributes(toolAgent.getSessionHandle(),
				toolAgent.getProcessInstanceId(), null, false).getArray()) {
			out.put(attr.getName(), attr.getValue());
		}

		// from the ToolAgent docs the assId parameter is the WorkItemId
		for (final WMAttribute attr : wapi.listWorkItemAttributes(toolAgent.getSessionHandle(),
				toolAgent.getProcessInstanceId(), toolAgent.getAssId(), null, false).getArray()) {
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

}
