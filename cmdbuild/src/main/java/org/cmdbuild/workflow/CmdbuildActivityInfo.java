package org.cmdbuild.workflow;

import static org.cmdbuild.workflow.WorkflowConstants.VarAdminStart;
import static org.cmdbuild.workflow.WorkflowConstants.VarQuickAccept;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.workflow.extattr.CmdbuildExtendedAttribute;
import org.cmdbuild.workflow.extattr.CmdbuildExtendedAttributeFactory;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttributeIterator;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.client.wfservice.XPDLBrowser;
import org.enhydra.shark.client.utilities.SharkWSFactory;
import org.enhydra.shark.utilities.WMEntityUtilities;

public class CmdbuildActivityInfo {

	protected CmdbuildActivityInfo(CmdbuildProcessInfo procInfo,
			WMEntity actEntity,
			WMSessionHandle handle, SharkWSFactory factory) throws Exception {
		adminStart = false;
		XPDLBrowser browser = factory.getXPDLBrowser();
		
		String[][] extAttrs = WMEntityUtilities.getExtAttribNVPairs(handle, browser, actEntity);
		
		for (String[] extAttr : extAttrs) {
			Log.WORKFLOW.trace("\tExt.Attr.["+extAttr[0]+"]: " + extAttr[1]);
			if(extAttr[0].equals(VarQuickAccept)){
				quickAcceptVariable = extAttr[1];
			} else if(extAttr[0].equalsIgnoreCase(VarAdminStart)){
				adminStart = true;
			} else if(CmdbuildExtendedAttributeFactory.getInstance().hasMapping(extAttr[0])) {
				Log.WORKFLOW.trace("found cmdbuild ext.attr. " + extAttr[0] + " for activity " + actEntity.getName());
				CmdbuildExtendedAttribute cea = CmdbuildExtendedAttributeFactory.getInstance().getExtAttr(extAttr[0]);
				cea.setup(extAttr[0], extAttr[1], extendedAttrs.size());
				extendedAttrs.add(cea);
			} else {
				WorkflowVariableType.putInList(extAttr, variables);
			}
		}
		this.activityName = actEntity.getName();
		this.activityId = actEntity.getActId();

		WMFilter flt = new WMFilter("Name", WMFilter.EQ, "Description");
		flt.setFilterType(XPDLBrowser.SIMPLE_TYPE_XPDL);
		WMAttribute[] attrs = browser.listAttributes(handle, actEntity, flt, false).getArray();
		if(attrs.length > 0){
			this.activityDescription = (String)attrs[0].getValue();
			Log.WORKFLOW.trace("activity '" + this.activityName + "': " + this.activityDescription);
		}
		if(adminStart) {
			Log.WORKFLOW.debug("Activity " + this.activityName + " is admin start");
			//set adminStartActivityId on procInfo
			procInfo.setAdminStartActivity(this.activityId);
		}
		
		WMFilter filter = new WMFilter("Name", WMFilter.EQ, "Performer");
		filter.setFilterType(XPDLBrowser.SIMPLE_TYPE_XPDL);
		WMAttributeIterator ei = browser.listAttributes(handle, actEntity, filter, true);
		WMAttribute out = (ei.hasNext()?ei.getArray()[0]:null);
		if(out != null) {
			Log.WORKFLOW.debug("performer of " + activityId + ": " + out.getValue());
			this.participantIdOrExpression = (String)out.getValue();
		}
	}
	
	boolean adminStart;
	String activityId;
	String activityName;
	String activityDescription;
	String quickAcceptVariable = null;
	String participantIdOrExpression;
	List<ActivityVariableDef> variables = new ArrayList<ActivityVariableDef>();
	List<CmdbuildExtendedAttribute> extendedAttrs = new ArrayList<CmdbuildExtendedAttribute>();
	
	public boolean isQuickAcceptActivity() {
		return null != quickAcceptVariable;
	}
	public String getQuickAcceptVariable() {
		return quickAcceptVariable;
	}
	
	public String getActivityDescription() {
		return activityDescription;
	}
	
	public String getActivityName() {
		return activityName;
	}
	
	public String getActivityId() {
		return activityId;
	}
	
	public boolean isAdminStart() {
		return adminStart;
	}
	
	public String getParticipantIdOrExpression() {
		return participantIdOrExpression;
	}
	
	public List<CmdbuildExtendedAttribute> getExtendedAttrs() {
		Log.WORKFLOW.debug("ext attrs of " + activityName + ", size: " + extendedAttrs.size());
		return extendedAttrs;
	}

	public List<ActivityVariable> getVariableInstances(ITable schema) {
		List<ActivityVariable> out = new LinkedList<ActivityVariable>();
		int index = 0;
		for (ActivityVariableDef def : variables) {
			try {
				out.add(new ActivityVariable(def, schema, index++));
			} catch (NotFoundException e) {
				Log.WORKFLOW.debug(String.format("skipping variable %s: not found in cmdbuild class", def.name));
			}
		}
		return out;
	}
	
}
