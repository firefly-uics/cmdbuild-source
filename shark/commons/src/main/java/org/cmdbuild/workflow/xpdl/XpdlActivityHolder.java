package org.cmdbuild.workflow.xpdl;

import java.util.List;

public interface XpdlActivityHolder {

	/**
	 * Creates and adds a new activity to this element
	 * 
	 * @param activity id
	 * @return the created activity
	 */
	public XpdlActivity createActivity(final String activityId);

	/**
	 * Get the starting activities for this element (those that have no
	 * incoming transition).
	 * 
	 * @return list of starting activities
	 */
	public List<XpdlActivity> getStartingActivities();

	public List<XpdlActivity> getStartingManualActivitiesRecursive();
}