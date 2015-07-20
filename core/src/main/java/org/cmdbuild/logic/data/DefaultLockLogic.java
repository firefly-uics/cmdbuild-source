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
		this.lockManager.lock(card(cardId));
	}

	@Override
	public void unlockCard(final Long cardId) {
		logger.debug(MARKER, "unlocking card '{}'", cardId);
		this.lockManager.unlock(card(cardId));
	}

	@Override
	public void unlockAllCards() {
		logger.debug(MARKER, "unlocking all cards");
		this.lockManager.unlockAll();
	}

	@Override
	public void lockActivity(final Long instanceId, final String activityId) {
		logger.debug(MARKER, "locking activity '{}' for instance '{}'", activityId, instanceId);
		this.lockManager.lock(instanceActivity(instanceId, activityId));
	}

	@Override
	public void unlockActivity(final Long instanceId, final String activityId) {
		logger.debug(MARKER, "unlocking activity '{}' for instance '{}'", activityId, instanceId);
		this.lockManager.unlock(instanceActivity(instanceId, activityId));
	}

	@Override
	public void unlockAllActivities() {
		logger.debug(MARKER, "unlocking all activities");
		this.lockManager.unlockAll();
	}

}
