package org.cmdbuild.logic.data.access.lock;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingLockManager extends ForwardingObject implements LockManager {

	/**
	 * Usable by subclasses only.
	 */
	public ForwardingLockManager() {
	}

	@Override
	protected abstract LockManager delegate();

	@Override
	public void lock(final Lockable lockable) {
		delegate().lock(lockable);
	}

	@Override
	public void unlock(final Lockable lockable) {
		delegate().unlock(lockable);
	}

	@Override
	public void unlockAll() {
		delegate().unlockAll();
	}

	@Override
	public void checkNotLocked(final Lockable lockable) {
		delegate().checkNotLocked(lockable);
	}

	@Override
	public void checkLockedbyUser(final Lockable lockable, final String userName) {
		delegate().checkLockedbyUser(lockable, userName);
	}

}
