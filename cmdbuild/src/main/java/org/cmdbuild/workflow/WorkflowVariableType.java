package org.cmdbuild.workflow;

import static org.cmdbuild.workflow.WorkflowConstants.VarToUpdate;
import static org.cmdbuild.workflow.WorkflowConstants.VarToUpdateRequired;
import static org.cmdbuild.workflow.WorkflowConstants.VarToView;

import java.util.List;

public enum WorkflowVariableType {
	VIEW(VarToView)
	,UPDATE(VarToUpdate)
	,REQUIRED(VarToUpdateRequired)
	;
	String extAttrName;
	private WorkflowVariableType(String eaName){extAttrName = eaName;}
	
	public static void putInList( String[] extAttr,List<ActivityVariableDef> activities ){
		for( WorkflowVariableType vt : values() ){
			if(vt.extAttrName.equals(extAttr[0])){
				activities.add(new ActivityVariableDef(vt,extAttr[1]));
				return;
			}
		}
	}
}
