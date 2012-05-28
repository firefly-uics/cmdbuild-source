package org.cmdbuild.workflow;

import java.util.List;

import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;

/**
 * Definition of a process activity.
 */
public interface CMActivity {

	public String getName();

	public String getDescription();

	public String getInstructions();

	/**
	 * Returns the performers defined for this activity.
	 * 
	 * @return list of defined performers
	 */
	public List<ActivityPerformer> getPerformers();

	/**
	 * Returns the first role performer defined for this activity.
	 * 
	 * @return role performer
	 */
	public ActivityPerformer getFirstRolePerformer();

	/**
	 * Returns an ordered list of variables to be displayed on the form.
	 * 
	 * @return
	 */
	public List<CMActivityVariableToProcess> getVariables();
//	public List<Widget> getWidgets();
}
