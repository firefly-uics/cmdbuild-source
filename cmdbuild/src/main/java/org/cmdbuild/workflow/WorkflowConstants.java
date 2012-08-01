package org.cmdbuild.workflow;

public class WorkflowConstants {
	
	public static final String ExtAttrProcessInitAct = "JaWE_GRAPH_START_OF_WORKFLOW";
	public static final String[] ExtAttrProcessIgnoreInitAct = {"FreeTextExpressionParticipant","SYSTEM"};

	public static final String ExtAttrProcessCMDBuildBindClass = "cmdbuildBindToClass";
	public static final String ExtAttrProcessUserStoppable = "userStoppable";
	
	public static final String ProcVarProcessClass = "ProcessClass";
	public static final String ProcVarProcessId = "ProcessId";
	public static final String ProcVarProcessCode = "ProcessCode";
	
	public static final String VarToView = "VariableToProcess_VIEW";
	public static final String VarToUpdate = "VariableToProcess_UPDATE";
	public static final String VarToUpdateRequired = "VariableToProcess_UPDATEREQUIRED";
	
	public static final String VarQuickAccept = "QuickAccept";
	public static final String VarAdminStart = "AdminStart";
	
	public static final String StateOpen = "open";
	public static final String StateClosed = "closed";
	
	public static final String StateOpenNotRunning = "open.not_running.not_started";
	public static final String StateOpenRunning = "open.running";
	public static final String StateClosedCompleted = "closed.completed";
	public static final String StateClosedTerminated = "closed.terminated";
	public static final String StateClosedAborted = "closed.aborted";

	public static final String AllState = "all";
	public static final String ProcessToStartId = "tostart";
}
