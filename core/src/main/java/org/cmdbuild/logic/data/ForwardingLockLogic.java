package org.cmdbuild.logic.data;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingLockLogic extends ForwardingObject implements LockLogic {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingLockLogic() {
	}

	@Override
	protected abstract LockLogic delegate();

	@Override
	public void lockCard(final Long cardId) {
		delegate().lockCard(cardId);
	}

	@Override
	public void unlockCard(final Long cardId) {
		delegate().unlockCard(cardId);
	}

	@Override
	public void checkNotLockedCard(final Long cardId) {
		delegate().checkNotLockedCard(cardId);
	}

	@Override
	public void checkCardLockedbyUser(final Long cardId, final String user) {
		delegate().checkCardLockedbyUser(cardId, user);
	}

	@Override
	public void lockActivity(final Long instanceId, final String activityId) {
		delegate().lockActivity(instanceId, activityId);
	}

	@Override
	public void unlockActivity(final Long instanceId, final String activityId) {
		delegate().unlockActivity(instanceId, activityId);
	}

	@Override
	public void checkActivityLockedbyUser(final Long instanceId, final String activityId, final String user) {
		delegate().checkActivityLockedbyUser(instanceId, activityId, user);
	}

	@Override
	public void checkNotLockedInstance(final Long instanceId) {
		delegate().checkNotLockedInstance(instanceId);
	}

	@Override
	public void unlockAll() {
		delegate().unlockAll();
	}

}
