package org.cmdbuild.shark.toolagent;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.workflow.ConfigurationHelper;
import org.cmdbuild.workflow.api.SharkWorkflowApiFactory;
import org.cmdbuild.workflow.api.WorkflowApi;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.jxpdl.XMLUtil;
import org.enhydra.jxpdl.XPDLConstants;
import org.enhydra.jxpdl.elements.ExtendedAttribute;
import org.enhydra.jxpdl.elements.ExtendedAttributes;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
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

	private static final String UNKNOWN_DESCRIPTION = EMPTY;
	private static final int UNKNOWN_CLASS_ID = -1;

	public interface ConditionEvaluator {

		void configure(CallbackUtilities cus) throws Exception;

		boolean evaluate();

	}

	private final ConditionEvaluator conditionEvaluator;

	private SharkWorkflowApiFactory workflowApiFactory;
	private volatile WorkflowApi workflowApi;

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

	public String getId() {
		return toolInfo.getId();
	}

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		super.configure(cus);

		conditionEvaluator.configure(cus);

		final ConfigurationHelper configurationHelper = new ConfigurationHelper(cus);
		workflowApiFactory = configurationHelper.getWorkflowApiFactory();
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
				setupWorkflowApi(shandle, procInstId);
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

	protected void setupWorkflowApi(final WMSessionHandle shandle, final String procInstId) {
		workflowApiFactory.setup(cus, shandle, procInstId);
	}

	public WorkflowApi getWorkflowApi() {
		if (workflowApi == null) {
			synchronized (this) {
				if (workflowApi == null) {
					workflowApi = workflowApiFactory.createWorkflowApi();
				}
			}
		}
		return workflowApi;
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

	/**
	 * Gets the application parameter or null if not present.
	 * 
	 * @param name
	 *            parameter name
	 * @return parameter or null
	 */
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
			if (XPDLConstants.FORMAL_PARAMETER_MODE_OUT.equals(p.the_mode)
					|| EXTENDED_ATTRIBUTES_PARAM.equals(p.the_formal_name)) {
				continue;
			}
			paramMap.put(p.the_formal_name, valueOf(p));
		}
		return paramMap;
	}

	private Object valueOf(final AppParameter parameter) {
		final Object rawValue = parameter.the_value;
		final Object value;
		if (parameter.the_class == ReferenceType.class) {
			final ReferenceType referenceType = ReferenceType.class.cast(rawValue);
			value = Long.toString(referenceType.getId());
		} else if (parameter.the_class == LookupType.class) {
			final LookupType lookupType = LookupType.class.cast(rawValue);
			value = Long.toString(lookupType.getId());
		} else {
			value = rawValue;
		}
		return value;
	}

	/**
	 * Decodes the XML because Shark 2.3 does not use jxpdl, so it would crash.
	 * 
	 * @param name
	 *            extended attribute key
	 * @return extended attribute value for that key
	 */
	protected final String getExtendedAttribute(final String name) {
		String value = null;
		try {
			final String eaXml = getParameterValue(EXTENDED_ATTRIBUTES_PARAM);
			final ExtendedAttributes eas = XMLUtil.destringyfyExtendedAttributes(eaXml);
			final ExtendedAttribute ea = eas.getFirstExtendedAttributeForName(name);
			if (ea != null) {
				value = ea.getVValue();
			}
		} catch (final Exception e) {
			cus.error(null, format("unable to get extended attribute '%s'", name));
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	public <T> T getProcessAttributeValue(final String name) throws Exception {
		final WMAttribute attribute = wapi().getProcessInstanceAttributeValue(shandle, procInstId, name);
		final Object value = attribute.getValue();
		return (T) value;
	}

	private WAPI wapi() throws Exception {
		return Shark.getInstance().getWAPIConnection();
	}

	protected final Object convertFromProcessValue(Object obj) {
		if (obj != null) {
			if (obj instanceof ReferenceType) {
				final ReferenceType ref = (ReferenceType) obj;
				if (ref.checkValidity()) {
					obj = ref.getId();
				}
			} else if (obj instanceof LookupType) {
				final LookupType loo = (LookupType) obj;
				if (loo.checkValidity()) {
					obj = loo.getId();
				}
			}
		}
		return obj;
	}

	protected final Object convertToProcessValue(final String stringValue, final Class<?> clazz) {
		if (stringValue == null) {
			return null;
		}
		if (Long.class.equals(clazz)) {
			return stringToLongOurWay(stringValue);
		} else if (ReferenceType.class.equals(clazz)) {
			final Long id = stringToLongOurWay(stringValue);
			if (id == null) {
				return new ReferenceType();
			} else {
				// TODO fetch the card from Class?
				return new ReferenceType(id.intValue(), UNKNOWN_CLASS_ID, UNKNOWN_DESCRIPTION);
			}
		} else if (LookupType.class.equals(clazz)) {
			final Long id = stringToLongOurWay(stringValue);
			if (id == null) {
				return new LookupType();
			} else {
				return getWorkflowApi().selectLookupById(id.intValue());
			}
		} else {
			return stringValue;
		}
	}

	private Long stringToLongOurWay(final String stringValue) {
		if (stringValue == null || stringValue.isEmpty()) {
			return null;
		}
		try {
			return Long.parseLong(stringValue);
		} catch (final NumberFormatException e) {
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
