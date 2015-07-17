package org.cmdbuild.logic.data.access.lock;

import org.cmdbuild.logic.data.access.lock.LockManager.Lockable;

public class Lockables {

	public static Lockable card(final Long id) {
		// TODO use cache
		return new LockableCard(id);
	}

	private Lockables() {
		// prevents instantiation
	}

}
