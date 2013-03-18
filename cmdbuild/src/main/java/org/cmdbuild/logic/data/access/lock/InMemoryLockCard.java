package org.cmdbuild.logic.data.access.lock;

import java.util.List;

import org.cmdbuild.exception.ConsistencyException;
import org.cmdbuild.exception.ConsistencyException.ConsistencyExceptionType;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.model.LockedCard;
import org.cmdbuild.services.store.LockedCardStore;
import org.cmdbuild.services.store.Store.Storable;

public class InMemoryLockCard implements LockCardManager {

	private boolean displayLockerUsername;
	private final LockedCardStore lockedCardStore;

	public InMemoryLockCard(final LockCardConfiguration configuration) {
		displayLockerUsername = configuration.isLockerUsernameVisible();
		lockedCardStore = new LockedCardStore(configuration.getExpirationTimeInMilliseconds());
	}

	@Override
	public synchronized void lock(Long cardId) {
		final LockedCard lockedCard = lockedCardStore.read(storable(cardId));
		boolean cardAlreadyLocked = lockedCard != null;
		if (cardAlreadyLocked) {
			throw createLockedCardException(lockedCard);
		}
		final LockedCard cardToLock = new LockedCard(cardId, getCurrentlyLoggedUsername());
		lockedCardStore.create(cardToLock);
	}

	private String getCurrentlyLoggedUsername() {
		return TemporaryObjectsBeforeSpringDI.getOperationUser().getAuthenticatedUser().getUsername();
	}

	@Override
	public synchronized void unlock(Long cardId) {
		final LockedCard lockedCard = lockedCardStore.read(storable(cardId));
		boolean cardNotExists = lockedCard == null;
		if (cardNotExists) {
			return;
		} else if (!lockedCard.getLockerUsername().equals(getCurrentlyLoggedUsername())) {
			createLockedCardException(lockedCard);
		} else {
			lockedCardStore.delete(storable(cardId));
		}
	}

	@Override
	public synchronized void unlockAll() {
		final List<LockedCard> lockedCards = lockedCardStore.list();
		for (LockedCard cardToUnlock : lockedCards) {
			lockedCardStore.delete(storable(cardToUnlock.getIdentifier()));
		}
	}

	@Override
	public void checkLockerUser(Long cardId, String userName) {
		final LockedCard lockedCard = lockedCardStore.read(storable(cardId));

		if (lockedCard != null && !lockedCard.getLockerUsername().equals(userName)) {
			throw createLockedCardException(lockedCard);
		}
	}

	private ConsistencyException createLockedCardException(final LockedCard lockedCard) {
		if (displayLockerUsername) {
			return ConsistencyExceptionType.LOCKED_CARD.createException(lockedCard.getLockerUsername(),
					"" + lockedCard.getTimeInSecondsSinceInsert());
		} else {
			return ConsistencyExceptionType.LOCKED_CARD.createException("undefined", "" + lockedCard.getTimeInSecondsSinceInsert());
		}
	}

	private Storable storable(final Long cardId) {
		return new Storable() {
			@Override
			public String getIdentifier() {
				return cardId.toString();
			}
		};
	}

	private Storable storable(final String cardId) {
		return new Storable() {
			@Override
			public String getIdentifier() {
				return cardId;
			}
		};
	}

	@Override
	public void updateLockCardConfiguration(final LockCardConfiguration configuration) {
		displayLockerUsername = configuration.isLockerUsernameVisible();
		lockedCardStore.setExpirationTime(configuration.getExpirationTimeInMilliseconds());
	}
}
