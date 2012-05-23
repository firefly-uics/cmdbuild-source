package org.cmdbuild.workflow.xpdl;

import java.util.ArrayList;
import java.util.List;

import org.enhydra.jxpdl.elements.Activities;
import org.enhydra.jxpdl.elements.Activity;

abstract class XpdlActivities {

	private final XpdlDocument doc;

	XpdlActivities(XpdlDocument doc) {
		this.doc = doc;
	}

	/**
	 * Creates and adds a new activity to this element
	 * 
	 * @param activity id
	 * @return the created activity
	 */
	public final XpdlActivity createActivity(final String activityId) {
		doc.turnReadWrite();
		Activity a = (Activity) activities().generateNewElement();
		a.setId(activityId);
		activities().add(a);
		return new XpdlActivity(process(), a);
	}

	/**
	 * Get the starting activities for this element (those that have no
	 * incoming transition).
	 * 
	 * @return list of starting activities
	 */
	public final List<XpdlActivity> getStartingActivities() {
		doc.turnReadOnly();
		List<XpdlActivity> startingActivities = new ArrayList<XpdlActivity>();
		for (final Activity a : startingActivities()) {
			startingActivities.add(new XpdlActivity(process(), a));
		}
		return startingActivities;
	}

	public final List<XpdlActivity> getStartingManualActivitiesRecursive() {
		List<XpdlActivity> out = new ArrayList<XpdlActivity>();
		for (XpdlActivity sa : getStartingActivities()) {
			if (sa.isManualType()) {
				out.add(sa);
			} else if (sa.isBlockType()) {
				out.addAll(sa.getBlockActivitySet().getStartingManualActivitiesRecursive());
			}
		}
		return out;
	}

	protected abstract XpdlProcess process();
	protected abstract Activities activities();
	protected abstract List<Activity> startingActivities();
}