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
		Object executeAction(String action, Map<String, Object> params, Map<String, Object> dsVars) throws Exception;
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
