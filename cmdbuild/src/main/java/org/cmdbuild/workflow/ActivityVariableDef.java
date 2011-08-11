package org.cmdbuild.workflow;

public class ActivityVariableDef {
	WorkflowVariableType type;
	String name;
	
	public ActivityVariableDef(WorkflowVariableType type, String name){
		this.type = type;
		this.name = name;
	}

	public WorkflowVariableType getType() {
		return type;
	}

	public String getName() {
		return name;
	}
}
