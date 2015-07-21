package org.cmdbuild.logic.data.access.lock;

import com.google.common.base.Optional;

public interface LockManager {

	interface Lockable {

		Optional<Lockable> parent();

	}

	void lock(Lockable lockable);

	void unlock(Lockable lockable);

	void unlockAll();

	void checkNotLocked(Lockable lockable);

	void checkLockedbyUser(Lockable lockable, String userName);

}
