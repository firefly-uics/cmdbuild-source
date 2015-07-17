package org.cmdbuild.logic.data.access.lock;

import org.cmdbuild.logic.data.access.lock.LockManager.Lockable;

public class Lockables {

	public static Lockable card(final Long id) {
		// TODO use cache
		return new LockableCard(id);
	}

	public static Lockable instanceActivity(final Long instanceId, final String activityId) {
		// TODO use cache
		return new LockableInstanceActivity(instanceId, activityId);
	}

	private Lockables() {
		// prevents instantiation
	}

}
