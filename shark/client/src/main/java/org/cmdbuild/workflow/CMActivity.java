package org.cmdbuild.workflow;

import java.util.List;

/**
 * Definition of a process activity.
 */
public interface CMActivity {

	public String getName();

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

}

