package org.cmdbuild.workflow.extattr;

import java.util.Map;

import org.cmdbuild.elements.WorkflowWidgetDefinition;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.SharkWSFacade;
import org.cmdbuild.workflow.operation.ActivityDO;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmc.wapi.WMWorkItem;
import org.enhydra.shark.client.utilities.SharkWSFactory;
import org.json.JSONException;
import org.json.JSONObject;

public interface CmdbuildExtendedAttribute {

	/**
	 * return true if the attributeName is the right one for this cmdb ext.attr.
	 * configure the extended attribute, called when creating the activities cache. <br>
	 * The index parameter identifies where to put the relative button in the js gui. <br>
	 * This method is called once.
	 * 
	 * @param attributeName
	 * @param attributeValue
	 * @param index
	 * @return
	 */
	boolean setup( String attributeName,String attributeValue, int index );
	
	/**
	 * do the necessary things and store the results in the given ActivityDO
	 * called when the current activity has the extended attribute, to configure it properly 
	 * and store the gui info (javascript needs to handle those infos and modify the gui). <br>
	 * This method is called whenether an activity which holds this ext.attrs. has to be serialized
	 * for user processing.
	 * 
	 * @param handle the current WMSessionHandle
	 * @param user the connected user
	 * @param role the connected user role
	 * @param factory
	 * @param workItem the current workitem
	 * @param activityDO the result object
	 * @throws Exception
	 */
	void configure( WMSessionHandle handle,
			UserContext userCtx,
			SharkWSFactory factory,
			WMWorkItem workItem,
			ActivityDO activityDO);
	
	/**
	 * called by the framework on a message to services/json/management/modworkflow/reactExtendedAttribute?identifier=extattridentifier...
	 * <br>
	 * Mainly used to store in the workflow some variable
	 * @param handle
	 * @param user
	 * @param role
	 * @param factory
	 * @param workItem
	 * @param activityDO
	 * @param requestParameters
	 * @throws Exception
	 */
	void react( WMSessionHandle handle,
			UserContext userCtx,
			SharkWSFactory factory,
			SharkWSFacade facade,
			WMWorkItem workItem,
			ActivityDO activityDO,
			Map<String, String[]> submissionParameters);
	
	/**
	 * build a string that identifies an extended attribute
	 * @return
	 */
	String identifier();
	
	/**
	 * where to put the ext attr configuration in the gui
	 * @return
	 */
	int index();
	
	/**
	 * get the label for the button
	 * @return
	 */
	String buttonLabel();
	
	/**
	 * The handled extended attribute Name
	 * @return
	 */
	String extendedAttributeName();
	
	/**
	 * put the previously filled activityDO properties in the out jsonobject.<br>
	 * (called after configure)
	 * @param activityDO
	 * @param object
	 */
	public JSONObject serializeJson(ActivityDO activityDO) throws JSONException;
	
	public WorkflowWidgetDefinition serialize(ActivityDO activityDO);
}
