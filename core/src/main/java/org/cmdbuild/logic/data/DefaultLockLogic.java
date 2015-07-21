package org.cmdbuild.logic.data;

import static org.cmdbuild.logic.data.access.lock.Lockables.card;
import static org.cmdbuild.logic.data.access.lock.Lockables.instanceActivity;

import org.cmdbuild.logic.data.access.lock.LockManager;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DefaultLockLogic implements LockLogic {

	private static final Marker MARKER = MarkerFactory.getMarker(LockLogic.class.getName());

	private final LockManager lockManager;

	public DefaultLockLogic(final LockManager lockManager) {
		this.lockManager = lockManager;
	}

	@Override
	public void lockCard(final Long cardId) {
		logger.debug(MARKER, "locking card '{}'", cardId);
		lockManager.lock(card(cardId));
	}

	@Override
	public void unlockCard(final Long cardId) {
		logger.debug(MARKER, "unlocking card '{}'", cardId);
		lockManager.unlock(card(cardId));
	}

	@Override
	public void checkNotLockedCard(final Long cardId) {
		logger.debug(MARKER, "checking if card '{}' is unlocked", cardId);
		lockManager.checkNotLocked(card(cardId));
	}

	@Override
	public void checkCardLockedbyUser(final Long cardId, final String user) {
		logger.debug(MARKER, "checking if card '{}' is locked by user '{}'", cardId, user);
		lockManager.checkLockedbyUser(card(cardId), user);
	}

	@Override
	public void lockActivity(final Long instanceId, final String activityId) {
		logger.debug(MARKER, "locking activity '{}' for instance '{}'", activityId, instanceId);
		lockManager.lock(instanceActivity(instanceId, activityId));
	}

	@Override
	public void unlockActivity(final Long instanceId, final String activityId) {
		logger.debug(MARKER, "unlocking activity '{}' for instance '{}'", activityId, instanceId);
		lockManager.unlock(instanceActivity(instanceId, activityId));
	}

	@Override
	public void checkActivityLockedbyUser(final Long instanceId, final String activityId, final String user) {
		logger.debug(MARKER, "checking if activity '{}' of instance '{}' is locked by user '{}'", activityId,
				instanceId, user);
		lockManager.checkLockedbyUser(instanceActivity(instanceId, activityId), user);
	}

	@Override
	public void unlockAll() {
		logger.debug(MARKER, "unlocking all cards");
		lockManager.unlockAll();
	}

}
