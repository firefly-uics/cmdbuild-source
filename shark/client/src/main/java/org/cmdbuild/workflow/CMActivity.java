package org.cmdbuild.workflow;

import java.util.List;
import java.util.Map;

import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;

/**
 * Definition of a process activity.
 */
public interface CMActivity {

	interface CMActivityWidget {
		String getId();
		String getLabel();
		boolean isAlwaysenabled();

		/**
		 * Performs some custom action.
		 * 
		 * @param action action type
		 * @param params action parameters
		 * @param dsVars values saved in the data store
		 * @return action output
		 * @throws Exception
		 */
		Object executeAction(String action, Map<String, Object> params, Map<String, Object> dsVars) throws Exception;

		/**
		 * Fills the output variables.
		 * 
		 * Values should be native to CMDBuild, not to the Workflow Engine.
		 * 
		 * @param input widget submission object
		 * @param output values to be saved into the workflow engine
		 * @throws Exception
		 */
		void save(CMActivityInstance activityInstance, Object input, Map<String, Object> output) throws Exception;

		/**
		 * React to the activity advancement.
		 * 
		 * This can be used by widgets that need to do something only when the
		 * activity advances, like sending emails, etc.
		 * 
		 * @throws Exception
		 */
		void advance(CMActivityInstance activityInstance);
	}

	String getId();

	String getDescription();

	String getInstructions();

	/**
	 * Returns the performers defined for this activity.
	 * 
	 * @return list of defined performers
	 */
	List<ActivityPerformer> getPerformers();

	/**
	 * Returns the first role performer defined for this activity.
	 * 
	 * @return role performer
	 */
	ActivityPerformer getFirstRolePerformer();

	/**
	 * Returns an ordered list of variables to be displayed on the form.
	 * 
	 * @return
	 */
	List<CMActivityVariableToProcess> getVariables();

	List<CMActivityWidget> getWidgets();
}
