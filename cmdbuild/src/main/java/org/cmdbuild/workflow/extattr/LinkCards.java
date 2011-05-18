package org.cmdbuild.workflow.extattr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.cql.compiler.impl.QueryImpl;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.utils.CQLFacadeCompiler;
import org.cmdbuild.workflow.SharkWSFacade;
import org.cmdbuild.workflow.operation.ActivityDO;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmc.wapi.WMWorkItem;
import org.enhydra.shark.client.utilities.SharkWSFactory;
import org.json.JSONObject;

public class LinkCards extends AbstractCmdbuildExtendedAttribute {
	static final String ClassName = "ClassName";
	static final String Filter = "Filter";
	static final String SingleSelect = "SingleSelect";
	static final String NoSelect = "NoSelect";
	static final String Required = "Required";

	public String extendedAttributeName() {
		return "linkCards";
	}

	@Override
	protected void doConfigure(WMSessionHandle handle, UserContext userCtx,
			SharkWSFactory factory, WMWorkItem workItem, ActivityDO activityDO,
			Map<String, Object> processVars,
			Map<String, Object> currentOutValues) {
	}

	@Override
	protected void doReact(WMSessionHandle handle, UserContext userCtx,
			SharkWSFactory factory, SharkWSFacade facade, WMWorkItem workItem,
			ActivityDO activityDO, Map<String, String[]> submissionParameters,
			ExtendedAttributeConfigParams eacp,
			Map<String, Object> outputParameters) {
		ITable targetClass;
		try {
			targetClass = getLinkTargetClass(eacp);
		} catch (Exception e) {
			throw WorkflowExceptionType.WF_CANNOT_REACT_CMDBEXTATTR.createException();
		}
		int[] refIds = (int[])outputParameters.get(this.outParameters.get(0));
		
		if(refIds.length == 0) {
			return;
		}
		String[] idVals = new String[refIds.length];
		int id = 0;
		for(int refId : refIds) {
			idVals[id] = refId + "";
			id++;
		}
		
		List<ReferenceType> newValueList = new ArrayList<ReferenceType>();
		for(ICard crd : targetClass.cards().list().filter("Id", AttributeFilterType.EQUALS, (Object[])idVals)) {
			newValueList.add(new ReferenceType(crd.getId(),crd.getIdClass(),crd.getDescription()));
		}
		
		ReferenceType[] newValue = newValueList.toArray(new ReferenceType[]{});
		
		Map<String,Object> newValues = new HashMap<String,Object>();
		newValues.put(this.outParameters.get(0), newValue);
		
		facade.updateWorkItemValues(handle, userCtx, factory, workItem, newValues);
	}

	protected void addCustomParams(ActivityDO activityDO, JSONObject object,
			ExtendedAttributeConfigParams eacp) throws Exception {
		ITable targetClass = getLinkTargetClass(eacp);
		object.put("ClassName", targetClass.getName());
		object.put("ClassId", targetClass.getId());
		object.put("outputName", this.outParameters.get(0));
	}

	private ITable getLinkTargetClass(ExtendedAttributeConfigParams eacp) throws Exception {
		Map<String,Object> params = eacp.getParameters();
		String cName;
		if (params.containsKey(Filter)) {
			String cqlQuery = (String)params.get(Filter);
			QueryImpl q = CQLFacadeCompiler.compileWithTemplateParams(cqlQuery);
			cName = q.getFrom().mainClass().getClassName();
		} else {
			cName = (String)params.get(ClassName);
		}
		return UserContext.systemContext().tables().get(cName);
	}

	@Override
	protected Object getOutputValue(String outParamName, WMSessionHandle handle,
			UserContext userCtx, SharkWSFactory factory, WMWorkItem workItem,
			ActivityDO activityDO, Map<String,String[]> submissionParameters) {
		return convertToIntArray(submissionParameters.get(outParamName));
	}

}
