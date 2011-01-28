package org.cmdbuild.workflow.extattr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.WorkflowWidgetDefinition;
import org.cmdbuild.elements.WorkflowWidgetDefinitionParameter;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.SharkWSFacade;
import org.cmdbuild.workflow.operation.ActivityDO;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmc.wapi.WMWorkItem;
import org.enhydra.shark.client.utilities.SharkWSFactory;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractCmdbuildExtendedAttribute implements
		CmdbuildExtendedAttribute {
	
	static final String Label = "ButtonLabel";
	static final String clientVarPrefix = "client:";
	private Map<String,String> map = new HashMap<String, String>();
	protected boolean configured = false;

	public class ExtendedAttributeConfigParams {
		Map<String,Object> parameters;
		Map<String,Object> currentOuts;
		
		@SuppressWarnings("unchecked")
		public ExtendedAttributeConfigParams(Map<String,String> staticParams,
				Map<String,Object> variableParams,
				Map<String,Object> currentOuts) {
			this.parameters = new HashMap();
			parameters.putAll(staticParams);
			parameters.putAll(variableParams);
			this.currentOuts = currentOuts;
		}
		
		public Map<String,Object> getParameters() { return parameters; }
		public Map<String,Object> getCurrentOuts() { return currentOuts; }
	}
	
	String extAttrName;
	/**
	 * Map of the ext.attr. static params (ie. name = 'value' or name = 100 /integer variables/)
	 */
	Map<String,String> staticParams;
	/**
	 * Map of the ext.attr. variable params (ie. name = someWFAttribute)
	 */
	Map<String,String> variableParams;
	/**
	 * List of ouput parameters
	 */
	List<String> outParameters;
	
	int guiIndex;
	
	String buttonLabel;
	
	/**
	 * The standard identifier for an ext.attr is:
	 * &lt;ext.attr.Name&gt;&lt;(ext.attr.Value.HashCode)&gt;
	 */
	String identifier;
	
	/**
	 * superclasses must use a default constructor to be called by the ext.attr. factory
	 * @param extAttrName
	 */
	public AbstractCmdbuildExtendedAttribute() {
		this.extAttrName = extendedAttributeName();
		this.staticParams = new HashMap<String,String>();
		this.variableParams = new HashMap<String,String>();
		this.outParameters = new ArrayList<String>();
		
		configured = false;
	}
	
	/**
	 * setup the extended attribute if the attributeName equals this.extAttrName
	 */
	public boolean setup(String attributeName, String attributeValue, int index) {
		if( this.extAttrName.equals(attributeName) ) {
			identifier = attributeName + (attributeValue.hashCode());
			Log.WORKFLOW.debug("setup " + attributeName + ", identifier: " + identifier);
			guiIndex = index;
			parseParameters( attributeValue );
			doSetup(attributeValue);
			return true;
		}
		return false;
	}
	
	/**
	 * Take the extended attribute "Value" string and parse it into static and variable parameters
	 * @param attributeValue
	 */
	protected void parseParameters( String attributeValue ) {
		StringTokenizer token1 = new StringTokenizer(attributeValue.trim(),"\r\n");
		while( token1.hasMoreTokens() ) {
			String tmp = token1.nextToken().trim();
			int eqIdx = isVar(tmp);
			if(-1 != eqIdx){
				//variable
				String key = tmp.substring(0,eqIdx).trim();
				String value = tmp.substring(eqIdx+1).trim();
				if (key!=null&&!key.equals("") && value!=null&&!value.equals("")){
					if (isProcVar(key, value)) {
						Log.WORKFLOW.trace("process var: " + key + " - map to " + value);
						variableParams.put(key, value);
					} else if (isClientVar(value)) {
						// adds brackets to the variable, so that it can be parsed as a template
						Log.WORKFLOW.trace("client var: " + key + " - map to client var: " + value);
						staticParams.put(key, String.format("{%s}", value));
					} else {
						value = getStrippedVar(value);
						Log.WORKFLOW.trace("static var: " + key + " = " + value);
						staticParams.put(key, value);
					}
				}
			} else {
				//this is an out parameter
				Log.WORKFLOW.trace("output var: " + tmp);
				outParameters.add(tmp);
			}
		}
		if(this.staticParams.containsKey(Label)) {
			this.buttonLabel = this.staticParams.get(Label);
		}
	}

	protected void doSetup( String attributeValue ){} //default empty implementation

	/**
	 * fill the map for process variables and call doHandle (inside a workflow operation)
	 */
	public void configure(WMSessionHandle handle, UserContext userCtx,
			SharkWSFactory factory, WMWorkItem workItem, ActivityDO activityDO) {
		Log.WORKFLOW.debug("configure "+extAttrName);
		
		Map<String,Object> processVars;
		Map<String,Object> currentOutVars;

		if(workItem != null) {
			try{ 
				WAPI wapi = factory.getWAPIConnection();
				processVars = getProcessVars(handle, workItem, wapi);
				currentOutVars = getOutputVars(handle, workItem, wapi);
			} catch(Exception e) {
				Log.WORKFLOW.error("error configuring ext attr " + this.extAttrName, e);
				throw WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR.createException(extAttrName);
			}
			if(processVars.containsKey(Label)) {
				this.buttonLabel = (String)processVars.get(Label);
			}
		} else {
			processVars = new HashMap<String, Object>();
			currentOutVars = new HashMap<String, Object>();
		}
		
		ExtendedAttributeConfigParams eacf = new ExtendedAttributeConfigParams(
				staticParams, processVars, currentOutVars);
		activityDO.setCmdbExtAttrParam(this.identifier(), eacf);
		doConfigure(handle,userCtx,factory,workItem,activityDO,processVars,currentOutVars);
		
		configured = true;
	}

	private Map<String, Object> getOutputVars(WMSessionHandle handle,
			WMWorkItem workItem, WAPI wapi) throws Exception {
		Map<String, Object> currentOutVars;
		currentOutVars = new HashMap<String, Object>();
		for (String key : this.outParameters) {
			Object currentValue = ((WMAttribute)
					wapi.getProcessInstanceAttributeValue(handle, workItem.getProcessInstanceId(), key)).getValue();
			Log.WORKFLOW.debug("set current " + key + " value: " + currentValue);
			currentOutVars.put(key, currentValue);
		}
		return currentOutVars;
	}

	private Map<String, Object> getProcessVars(WMSessionHandle handle,
			WMWorkItem workItem, WAPI wapi) throws Exception {
		Map<String, Object> processVars;
		processVars = new HashMap<String, Object>();
		for (String key : this.variableParams.keySet()) {
			String processVarName = this.variableParams.get(key);
			try {
				Object processVarValue = ((WMAttribute) wapi.getProcessInstanceAttributeValue(handle, workItem.getProcessInstanceId(), processVarName)).getValue();
				processVars.put(key, processVarValue);
			} catch (Exception e) {
				Log.WORKFLOW.error(String.format("Can't get process variable \"%s\"", processVarName));
			}
		}
		return processVars;
	}

	String printAttributeValue(AttributeValue value) {
		Integer id = value.getId();
		if (id != null) {
			return id.toString();
		} else {
			return value.toString();
		}
	}

	/**
	 * subclasses have to implement this method which is called by the handle method
	 * @param handle
	 * @param user
	 * @param role
	 * @param factory
	 * @param workItem
	 * @param activityDO
	 * @param processVars
	 * @throws Exception
	 */
	protected abstract void doConfigure(WMSessionHandle handle, UserContext userCtx,
			SharkWSFactory factory, WMWorkItem workItem, ActivityDO activityDO,
			Map<String,Object> processVars, Map<String,Object> currentOutValues);
	
	/**
	 * @see CmdbuildExtendedAttribute.react
	 * @param handle
	 * @param user
	 * @param role
	 * @param factory
	 * @param workItem
	 * @param activityDO
	 * @param requestParameters
	 * @throws Exception
	 */
	public void react(WMSessionHandle handle, UserContext userCtx,
			SharkWSFactory factory, SharkWSFacade facade,
			WMWorkItem workItem, ActivityDO activityDO,
			Map<String, String[]> submissionParameters) {
		Log.WORKFLOW.debug("react "+extAttrName);
		if(!configured) {return;}
		Map<String,Object> outValues = new HashMap<String,Object>();
		for(String outParamName : outParameters) {
			Object outParamValue = this.getOutputValue(outParamName, handle, userCtx, factory, workItem, activityDO, submissionParameters);
			outValues.put(outParamName, outParamValue);
		}
		ExtendedAttributeConfigParams eacp = (ExtendedAttributeConfigParams)activityDO.getCmdbExtAttrParam(identifier());
		this.doReact(handle, userCtx, factory, facade, workItem, activityDO, submissionParameters, eacp, outValues);
	}
	
	/**
	 * called by {@link react} after got all the output parameters
	 * @param handle
	 * @param user
	 * @param role
	 * @param factory
	 * @param workItem
	 * @param activityDO
	 * @param requestParameters
	 * @param outputParameters
	 * @throws Exception
	 */
	protected void doReact(WMSessionHandle handle, UserContext userCtx,
			SharkWSFactory factory, SharkWSFacade facade,
			WMWorkItem workItem, ActivityDO activityDO,
			Map<String, String[]> submissionParameters,
			ExtendedAttributeConfigParams oldConfig,
			Map<String, Object> outputParameters) {
	}
	
	/**
	 * Retrieve an output value
	 * @param outParamName
	 * @param handle
	 * @param user
	 * @param role
	 * @param factory
	 * @param workItem
	 * @param activityDO
	 * @param requestParameters
	 * @return
	 * @throws Exception
	 */
	protected Object getOutputValue(String outParamName,
			WMSessionHandle handle, UserContext userCtx,
			SharkWSFactory factory, WMWorkItem workItem, ActivityDO activityDO,
			Map<String, String[]> submissionParameters) {
		return null;
	}	
	
	/**
	 * build an identifier for this extended attribute. it will be used to bind the ext.attr. to the client
	 */
	public String identifier() {
		return this.identifier;
	}
	
	public int index() {
		return this.guiIndex;
	}
	
	@SuppressWarnings("unchecked")
	public WorkflowWidgetDefinition serialize(ActivityDO activityDO){
		WorkflowWidgetDefinition wwd = new WorkflowWidgetDefinition(this.extendedAttributeName(), this.identifier());
		JSONObject json = serializeParamsAsJson(activityDO);
		Iterator<String> i = json.keys();
		while (i.hasNext()){
			String key = i.next();
			try {
				String value = json.get(key).toString();
				addDefinitionParameter(key, value);
			} catch (JSONException e) {
				Log.WORKFLOW.debug("Error accessing element with key " + key);
			}
		}
		wwd.setParameters(getWorkflowWidgetDefinitionParameterList());
		return wwd;
	}
	
	private void addDefinitionParameter(String key, String value) {
		map.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	private List<WorkflowWidgetDefinitionParameter> getWorkflowWidgetDefinitionParameterList() {
		Iterator iteratorMap = map.entrySet().iterator();
		List<WorkflowWidgetDefinitionParameter> wwdpList = new ArrayList<WorkflowWidgetDefinitionParameter>(); 
		while(iteratorMap.hasNext()){
		      Map.Entry me = (Map.Entry)iteratorMap.next();
		      WorkflowWidgetDefinitionParameter wwdp = new WorkflowWidgetDefinitionParameter();
		      wwdp.setKey((String) me.getKey());
		      wwdp.setValue((String)me.getValue());
		      wwdpList.add(wwdp);
		}
		return wwdpList;
	}
	
	public final JSONObject serializeJson(ActivityDO activityDO) {
		try {
			JSONObject extAttrObj = serializeParamsAsJson(activityDO);
			if (configured) {
				extAttrObj.put("extattrtype", this.extendedAttributeName());
				extAttrObj.put("identifier", this.identifier());
				extAttrObj.put("btnLabel", this.buttonLabel());
			}
			return extAttrObj;
		} catch (Exception e) {
			return new JSONObject();
		}
	}

	public final JSONObject serializeParamsAsJson(ActivityDO activityDO) {
		try {
			JSONObject extAttrObj = new JSONObject();
			if (configured) {
				ExtendedAttributeConfigParams eacp = (ExtendedAttributeConfigParams)activityDO.getCmdbExtAttrParam(identifier());
				addInputParams(extAttrObj, eacp);
				addCustomParams(activityDO, extAttrObj, eacp);
			}
			return extAttrObj;
		} catch (Exception e) {
			return new JSONObject();
		}
	}

	private void addInputParams(JSONObject object, ExtendedAttributeConfigParams eacp) throws JSONException {
		Map<String, Object> params = eacp.getParameters();
		for (String key : params.keySet()) {
			object.put(key, params.get(key));
		}
	}

	protected void addCustomParams(ActivityDO activityDO, JSONObject object, ExtendedAttributeConfigParams eacp) throws Exception {
	}

	public String buttonLabel() {
		return this.buttonLabel;
	}

	/**
	 * return the index of the char '='
	 * @param s
	 * @return
	 */
	private int isVar(String s){ return s.indexOf('='); }
	
	/**
	 * true if the value does not starts with quotation
	 * @param value
	 * @return
	 */
	private boolean isProcVar(String key, String value){
		return !(key.equals("Filter") || // For backward compatibility
				value.startsWith("\"") ||
				value.startsWith("'") || 
				value.startsWith(clientVarPrefix) ||
				isNumeric(value));
	}
	
	private boolean isNumeric(String s) {
		try{
			Integer.parseInt(s);
			return true;
		} catch(Exception e){}
		return false;
	}
	
	private boolean isClientVar(String s) {
		return s.startsWith(clientVarPrefix);
	}
	
	/**
	 * remove starting/trailing quotations
	 * @param value
	 * @return
	 */
	private String getStrippedVar(String value){
		if ((value.startsWith("\"") && value.endsWith("\""))
				|| (value.startsWith("'") && value.endsWith("'"))) {
			value = value.substring(1, value.length()-1);
		}
		return value;
	}
	
	protected final int[] convertToIntArray(String[] stringValues) {
		if (stringValues != null) {
			int[] intValues = new int[stringValues.length];
			for (int i=0; i<stringValues.length; ++i) {
				try {
					intValues[i] = Integer.valueOf(stringValues[i]);
				} catch (NumberFormatException e) {
					return new int[]{};
				}
			}
			return intValues;
		} else {
			return new int[0];
		}
	}

	protected final Integer convertToInt(String[] stringValues) {
		if (stringValues != null && stringValues.length > 0) {
			return Integer.valueOf(stringValues[0]);
		} else {
			return 0;
		}
	}

	protected final String firstStringOrNull(String[] stringValues) {
		if (stringValues != null && stringValues.length > 0) {
			return stringValues[0];
		} else {
			return null;
		}
	}
}
