package org.cmdbuild.logic.data.access.lock;

import org.cmdbuild.exception.ConsistencyException;

public interface LockCardManager {

	interface LockCardConfiguration {

		boolean isLockerUsernameVisible();

		long getExpirationTimeInMilliseconds();

	}

	void lock(Long cardId);

	void unlock(Long cardId);

	void unlockAll();

	void checkLockerUser(final Long cardId, final String userName) throws ConsistencyException;
}
