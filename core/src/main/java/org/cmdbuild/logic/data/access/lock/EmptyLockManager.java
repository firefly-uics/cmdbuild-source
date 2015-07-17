package org.cmdbuild.logic.data.access.lock;

public class EmptyLockManager implements LockManager {

	@Override
	public void lock(final Lockable lockable) {
	}

	@Override
	public void unlock(final Lockable lockable) {
	}

	@Override
	public void unlockAll() {
	}

	@Override
	public void checkLockedbyUser(final Lockable lockable, final String userName) {
	}

	@Override
	public void checkNotLocked(final Lockable lockable) {
	}

}
