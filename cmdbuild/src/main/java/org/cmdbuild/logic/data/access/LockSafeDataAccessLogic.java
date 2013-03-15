package org.cmdbuild.logic.data.access;

import java.util.List;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.ConsistencyException;
import org.cmdbuild.exception.ConsistencyException.ConsistencyExceptionType;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.model.LockedCard;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.services.store.LockedCardStore;
import org.cmdbuild.services.store.Store.Storable;

public class LockSafeDataAccessLogic extends DataAccessLogic {

	/**
	 * TODO: move it to a separate file which contains the configurations read
	 * for locked cards
	 */
	interface LockCardConfiguration {

		boolean isLockerUsernameVisible();

		long getExpirationTimeInMilliseconds();

	}

	private final boolean displayLockerUsername;
	private final LockedCardStore lockedCardStore;
	private final String currentlyLoggedUsername;

	public LockSafeDataAccessLogic(final CMDataView view, final LockCardConfiguration configuration) {
		super(view);
		displayLockerUsername = configuration.isLockerUsernameVisible();
		lockedCardStore = new LockedCardStore(configuration.getExpirationTimeInMilliseconds());
		currentlyLoggedUsername = TemporaryObjectsBeforeSpringDI.getOperationUser().getAuthenticatedUser()
				.getUsername();
	}

	@Override
	public synchronized void updateCard(final Card card) {
		final LockedCard lockedCard = lockedCardStore.read(storable(card.getId()));
		if (lockedCard != null && lockedCard.getLockerUsername().equals(currentlyLoggedUsername)) {
			super.updateCard(card);
			unlockCard(card.getId());
		} else if (lockedCard != null && !lockedCard.getLockerUsername().equals(currentlyLoggedUsername)) {
			createLockedCardException(lockedCard);
		} else if (lockedCard == null) {
			super.updateCard(card);
		}
	}

	public synchronized void lockCard(final Long cardId) {
		final LockedCard lockedCard = lockedCardStore.read(storable(cardId));
		boolean cardAlreadyLocked = lockedCard != null;
		if (cardAlreadyLocked) {
			throw createLockedCardException(lockedCard);
		}
		final LockedCard cardToLock = new LockedCard(cardId, currentlyLoggedUsername);
		lockedCardStore.create(cardToLock);
	}

	public void unlockCard(final Long cardId) {
		final LockedCard lockedCard = lockedCardStore.read(storable(cardId));
		boolean cardNotExists = lockedCard == null;
		if (cardNotExists) {
			return;
		} else if (!lockedCard.getLockerUsername().equals(currentlyLoggedUsername)) {
			createLockedCardException(lockedCard);
		} else {
			lockedCardStore.delete(storable(cardId));
		}
	}

	public void unlockAllCards() {
		final List<LockedCard> lockedCards = lockedCardStore.list();
		for (LockedCard cardToUnlock : lockedCards) {
			lockedCardStore.delete(storable(cardToUnlock.getIdentifier()));
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

	private ConsistencyException createLockedCardException(final LockedCard lockedCard) {
		if (displayLockerUsername) {
			return ConsistencyExceptionType.LOCKED_CARD.createException(lockedCard.getLockerUsername(),
					"" + lockedCard.getTimeInSecondsSinceInsert());
		} else {
			return ConsistencyExceptionType.LOCKED_CARD.createException("" + lockedCard.getTimeInSecondsSinceInsert());
		}
	}
}
