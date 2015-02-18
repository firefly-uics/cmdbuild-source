package org.cmdbuild.logic.data.access.lock;

import static org.cmdbuild.data.store.Storables.storableOf;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.exception.ConsistencyException;
import org.cmdbuild.exception.ConsistencyException.ConsistencyExceptionType;
import org.cmdbuild.model.LockedCard;
import org.cmdbuild.services.store.LockedCardStore;

public class InMemoryLockCard implements LockCardManager {

	private boolean displayLockerUsername;
	private final LockedCardStore lockedCardStore;
	private final OperationUser operationUser;

	public InMemoryLockCard(final LockCardConfiguration configuration, final OperationUser operationUser,
			final LockedCardStore lockedCardStore) {
		this.displayLockerUsername = configuration.isLockerUsernameVisible();
		this.operationUser = operationUser;
		this.lockedCardStore = lockedCardStore;
	}

	@Override
	public synchronized void lock(final Long cardId) {
		final LockedCard lockedCard = lockedCardStore.read(storableOf(cardId));
		final boolean cardAlreadyLocked = lockedCard != null;
		if (cardAlreadyLocked && !getCurrentlyLoggedUsername().equals(lockedCard.getLockerUsername())) {

			throw createLockedCardException(lockedCard);
		}
		final LockedCard cardToLock = new LockedCard(cardId, getCurrentlyLoggedUsername());
		lockedCardStore.create(cardToLock);
	}

	private String getCurrentlyLoggedUsername() {
		return operationUser.getAuthenticatedUser().getUsername();
	}

	@Override
	public synchronized void unlock(final Long cardId) {
		final LockedCard lockedCard = lockedCardStore.read(storableOf(cardId));
		final boolean cardNotExists = lockedCard == null;
		if (cardNotExists) {
			return;
		} else if (!lockedCard.getLockerUsername().equals(getCurrentlyLoggedUsername())) {
			createLockedCardException(lockedCard);
		} else {
			lockedCardStore.delete(storableOf(cardId));
		}
	}

	@Override
	public synchronized void unlockAll() {
		for (final LockedCard cardToUnlock : lockedCardStore.readAll()) {
			lockedCardStore.delete(storableOf(cardToUnlock.getIdentifier()));
		}
	}

	@Override
	public void checkLockerUser(final Long cardId, final String userName) {
		final LockedCard lockedCard = lockedCardStore.read(storableOf(cardId));

		if (lockedCard != null && !lockedCard.getLockerUsername().equals(userName)) {
			throw createLockedCardException(lockedCard);
		}
	}

	@Override
	public void checkLocked(final Long cardId) {
		final LockedCard lockedCard = lockedCardStore.read(storableOf(cardId));
		if (lockedCard != null) {
			throw createLockedCardException(lockedCard);
		}
	}

	private ConsistencyException createLockedCardException(final LockedCard lockedCard) {
		if (displayLockerUsername) {
			return ConsistencyExceptionType.LOCKED_CARD.createException(lockedCard.getLockerUsername(),
					"" + lockedCard.getTimeInSecondsSinceInsert());
		} else {
			return ConsistencyExceptionType.LOCKED_CARD.createException("undefined",
					"" + lockedCard.getTimeInSecondsSinceInsert());
		}
	}

	@Override
	public void updateLockCardConfiguration(final LockCardConfiguration configuration) {
		displayLockerUsername = configuration.isLockerUsernameVisible();
		lockedCardStore.setExpirationTime(configuration.getExpirationTimeInMilliseconds());
	}
}
