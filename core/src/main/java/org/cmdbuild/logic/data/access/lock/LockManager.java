package org.cmdbuild.logic.data.access.lock;

import static java.lang.String.format;

import java.util.Date;

public interface LockManager {

	@SuppressWarnings("serial")
	class ExpectedLocked extends Exception {

		public ExpectedLocked() {
			super("should be locked");
		}

	}

	@SuppressWarnings("serial")
	class LockedByAnotherUser extends Exception {

		private final String user;
		private final Date time;

		public LockedByAnotherUser(final String user, final Date time) {
			super(format("locked by user '%s' since '%s'", user, time));
			this.user = user;
			this.time = time;
		}

		public String getUser() {
			return user;
		}

		public Date getTime() {
			return time;
		}

	}

	void lock(Lockable lockable) throws LockedByAnotherUser;

	void unlock(Lockable lockable) throws LockedByAnotherUser;

	void unlockAll();

	void checkNotLocked(Lockable lockable) throws LockedByAnotherUser;

	void checkLockedByUser(Lockable lockable, String userName) throws LockedByAnotherUser, ExpectedLocked;

}
