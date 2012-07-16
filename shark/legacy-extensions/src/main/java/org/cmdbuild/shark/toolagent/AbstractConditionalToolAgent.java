package org.cmdbuild.shark.toolagent;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.workflow.api.SchemaApi;
import org.cmdbuild.workflow.api.SharkWorkflowApi;
import org.cmdbuild.workflow.api.WorkflowApi;
import org.enhydra.jxpdl.XMLUtil;
import org.enhydra.jxpdl.XPDLConstants;
import org.enhydra.jxpdl.elements.ExtendedAttribute;
import org.enhydra.jxpdl.elements.ExtendedAttributes;
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

	private static final String EXTENDED_ATTRIBUTES_PARAM = "ExtendedAttributes"; 
	private static final String CMDBUILD_API_CLASSNAME_PROPERTY = "org.cmdbuild.workflow.api.classname";

	public static interface ConditionEvaluator {

		void configure(CallbackUtilities cus) throws Exception;

		boolean evaluate();

	}

	private final ConditionEvaluator conditionEvaluator;
	private SharkWorkflowApi workflowApi;

	public AbstractConditionalToolAgent() {
		conditionEvaluator = new SharkConditionalEvaluator(this);
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

	public SchemaApi getSchemaApi() {
		return workflowApi;
	}

	public String getId() {
		return toolInfo.getId();
	}

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		super.configure(cus);
		configureApi(cus);
		conditionEvaluator.configure(cus);
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
			if (conditionEvaluator.evaluate()) {
				innerInvoke();
			}
			setStatus(APP_STATUS_FINISHED);
		} catch (final Exception e) {
			setStatus(APP_STATUS_INVALID);
			throw new ToolAgentGeneralException(e);
		}
	}

	protected void setStatus(final long status) {
		this.status = status;
	}

	protected abstract void innerInvoke() throws Exception;

	@SuppressWarnings("unchecked")
	public <T> T getParameterValue(final String name) {
		final Object value = getParameterOrDie(name).the_value;
		return (T) value;
	}

	public <T> void setParameterValue(final String name, final T value) {
		getParameter(name).the_value = value;
	}

	private AppParameter getParameterOrDie(final String name) {
		final AppParameter parameter = getParameter(name);
		if (parameter == null) {
			throw new IllegalArgumentException(format("missing parameter for name '%s'", name));
		} else {
			return parameter;
		}
	}

	public boolean hasParameter(final String name) {
		return (getParameter(name) != null);
	}

	private AppParameter getParameter(final String name) {
		for (final AppParameter parameter : parameters) {
			final String formalName = parameter.the_formal_name;
			if (formalName.equals(name)) {
				return parameter;
			}
		}
		return null;
	}

	/**
	 * Get IN and INOUT parameter values
	 */
	protected final Map<String, Object> getInputParameterValues() {
		final Map<String, Object> paramMap = new HashMap<String, Object>();
		for (final AppParameter p : parameters) {
			if (XPDLConstants.FORMAL_PARAMETER_MODE_OUT.equals(p.the_mode) ||
					EXTENDED_ATTRIBUTES_PARAM.equals(p.the_formal_name)) {
				continue;
			}
			final String formalName = p.the_formal_name;
			final Object value = p.the_value;
			paramMap.put(formalName, value);
		}
		return paramMap;
	}

	/**
	 * Decodes the XML because Shark 2.3 does not use jxpdl, so it would crash.
	 * 
	 * @param name extended attribute key
	 * @return extended attribute value for that key
	 */
	protected final String getExtendedAttribute(final String name) {
		try {
			final String eaXml = getParameterValue(EXTENDED_ATTRIBUTES_PARAM);
			final ExtendedAttributes eas = XMLUtil.destringyfyExtendedAttributes(eaXml);
			final ExtendedAttribute ea = eas.getFirstExtendedAttributeForName(name);
			return ea.getVValue();
		} catch (final Exception e) {
			cus.error(null, format("unable to get extended attribute '%s'", name));
			return null;
		}
	}
}

class CardRef {
	final String className;
	final int cardId;

	public CardRef(final String className, final int cardId) {
		this.className = className;
		this.cardId = cardId;
	}
}
