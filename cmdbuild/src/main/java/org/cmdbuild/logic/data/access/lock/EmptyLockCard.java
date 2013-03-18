package org.cmdbuild.logic.data.access.lock;

import org.cmdbuild.model.LockedCard;

public class EmptyLockCard implements LockCardManager {

	@Override
	public void lock(Long cardId) {
	}

	@Override
	public void unlock(Long cardId) {
	}

	@Override
	public void unlockAll() {
	}

	@Override
	public void checkLockerUser(Long cardId, String userName) {
	}

	@Override
	public void checkLocked(Long cardId) {
	}

	@Override
	public void updateLockCardConfiguration(LockCardConfiguration configuration) {
	}

}
