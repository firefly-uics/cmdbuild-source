package org.cmdbuild.workflow;

import static org.cmdbuild.workflow.WorkflowConstants.ExtAttrProcessCMDBuildBindClass;
import static org.cmdbuild.workflow.WorkflowConstants.ExtAttrProcessIgnoreInitAct;
import static org.cmdbuild.workflow.WorkflowConstants.ExtAttrProcessInitAct;
import static org.cmdbuild.workflow.WorkflowConstants.ExtAttrProcessUserStoppable;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.client.wfservice.XPDLBrowser;
import org.enhydra.shark.client.utilities.SharkWSFactory;
import org.enhydra.shark.utilities.WMEntityUtilities;

public class CmdbuildProcessInfo {
	
	protected CmdbuildProcessInfo (WMEntity procEntity,
			WMSessionHandle handle, SharkWSFactory factory) throws Exception {
		
		this.procEntity = procEntity;
		initialUserRoleActivity = new ArrayList<PerformerActivity>();
		
		XPDLBrowser browser = factory.getXPDLBrowser();
		
		participants = WMEntityUtilities.getAllParticipants(handle, browser, procEntity);
		
		String[][] extAttrs = WMEntityUtilities.getExtAttribNVPairs(handle, browser, procEntity);
		for(String[] extAttr : extAttrs) {
			if( extAttr[0].equals(ExtAttrProcessUserStoppable) ){
				this.userStoppable = Boolean.parseBoolean(extAttr[1]);
			} else if( extAttr[0].equals(ExtAttrProcessCMDBuildBindClass) ){
				this.cmdbuildBindedClass = extAttr[1];
			} else if( extAttr[0].equals(ExtAttrProcessInitAct) ){
				String v_value = extAttr[1];
				int bidx = v_value.indexOf("CONNECTING_ACTIVITY_ID=");
				int eidx = v_value.indexOf(",",bidx);
				String activity = v_value.substring(bidx+23,eidx);
				
				bidx = v_value.indexOf("JaWE_GRAPH_PARTICIPANT_ID=");
				eidx = v_value.indexOf(",", bidx);
				String performer = v_value.substring(bidx+26, eidx);
				
				Log.WORKFLOW.debug("check participant: " + performer + " for activity " + activity);
				for(WMEntity part : participants){
					if(!isIgnoredParticipant(part.getId())){
						if(part.getId().equals(performer)){
							WMEntity ptype = WMEntityUtilities.getSubEntity(handle, browser, part, "ParticipantType");
							String type = WMEntityUtilities.getAttributeValue(handle, browser, ptype, "Type");
							initialUserRoleActivity.add( new PerformerActivity(type,performer,activity) );
							Log.WORKFLOW.debug("add initial performer: " + performer + ", type: " + type);
						}
					}
				}
			}
		}
	}
	
	private boolean isIgnoredParticipant( String partId ) {
		for(String ignored : ExtAttrProcessIgnoreInitAct){
			if(ignored.equalsIgnoreCase(partId))
				return true;
		}
		return false;
	}

	public enum PerformerType {
		HUMAN,
		ROLE;
	}
	public class PerformerActivity {
		public PerformerActivity(String type, String id, String actId) {
			this.identifier = id;
			this.type = PerformerType.valueOf( type.toUpperCase() );
			this.activityId = actId;
		}
		PerformerType type;
		String identifier;
		String activityId;
		public boolean canBeRunBy(String group, String user) {
			switch (type) {
			case HUMAN:
				return this.identifier.equals(user);
			case ROLE:
				return this.identifier.equals(group);
			default:
				return false;
			}
		}
		boolean canBeRunBy(UserContext userCtx) {
			switch (type) {
			case HUMAN:
				return this.identifier.equals(userCtx.getUsername());
			case ROLE:
				return userCtx.belongsTo(this.identifier);
			default:
				return false;
			}
		}
		public String getActivityId() {
			return this.activityId;
		}
	}
	
	WMEntity procEntity;
	boolean userStoppable;
	String cmdbuildBindedClass = "";
	List<PerformerActivity> initialUserRoleActivity;
	public boolean isMultiStart(){
		return initialUserRoleActivity.size() > 1;
	}
	
	String adminStartActivity = null;
	
	WMEntity[] participants;
	
	public WMEntity getProcessEntity(){
		return procEntity;
	}
	public String getPackageId() {
		return procEntity.getPkgId();
	}
	public String getPackageVersion() {
		return procEntity.getPkgVer();
	}
	
	public WMEntity[] getParticipants() {
		return participants;
	}
	
	public void setAdminStartActivity(String adminStartActivity) {
		this.adminStartActivity = adminStartActivity;
	}
	
	public boolean isUserStoppable() {
		return userStoppable;
	}
	public String getCmdbuildBindedClass() {
		return cmdbuildBindedClass;
	}
	public List<PerformerActivity> getInitialUserRole() {
		return initialUserRoleActivity;
	}

	public boolean hasInitialUserRole(UserContext userCtx) {
		return (getInitialActivityIdFor(userCtx) != null);
	}

	public boolean isStartingActivity(String activityDefId) {
		for(PerformerActivity p : initialUserRoleActivity) {
			if(p.activityId.equals(activityDefId))
				return true;
		}
		return false;
	}

	public String getInitialActivityIdFor(UserContext userCtx) {
		if (userCtx.privileges().isAdmin()) {
			if (this.adminStartActivity == null && isSingleStart()) {
				// only 1 activity, treat as adminstart too
				this.adminStartActivity = this.initialUserRoleActivity.get(0)
						.getActivityId();
			}

			if (this.adminStartActivity == null) {
				WorkflowCache.getInstance().getActivityInfo(null,
						this.procEntity);
			}
			Log.WORKFLOW.debug("admin connected, return adminStartActivity: "
					+ this.adminStartActivity);
			return this.adminStartActivity;
		} else if (isSingleStart()) {
			PerformerActivity singleActivity = this.initialUserRoleActivity.get(0);
			if (singleActivity.canBeRunBy(userCtx)) {
				return singleActivity.getActivityId();
			}
		} else {
			String username = userCtx.getUsername();
			String defaultWfGroupName = userCtx.getWFStartGroup().getName();
			Log.WORKFLOW.debug("search start activity for: " + username + "/" + defaultWfGroupName);
			for (PerformerActivity pa : initialUserRoleActivity) {
				if (pa.canBeRunBy(defaultWfGroupName, username)) {
					return pa.getActivityId();
				}
			}
		}
		return null;
	}

	private boolean isSingleStart() {
		return this.initialUserRoleActivity.size() == 1;
	}
	
	
}
