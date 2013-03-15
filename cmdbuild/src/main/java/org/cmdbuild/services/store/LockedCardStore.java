package org.cmdbuild.services.store;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.cmdbuild.model.LockedCard;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

public class LockedCardStore implements Store<LockedCard> {

	private final Cache<String, LockedCard> lockedCards;

	/**
	 * 
	 * @param expirationTimeInMilliseconds
	 *            after this period of time the store is cleared
	 */
	public LockedCardStore(final long expirationTimeInMilliseconds) {
		lockedCards = CacheBuilder.newBuilder() //
				.expireAfterWrite(expirationTimeInMilliseconds, TimeUnit.MILLISECONDS) //
				.build();
	}

	@Override
	public Storable create(LockedCard lockedCard) {
		lockedCard.setLockTimestamp(new Date());
		lockedCards.put(lockedCard.getIdentifier(), lockedCard);
		return lockedCard;
	}

	@Override
	public LockedCard read(Storable storable) {
		return lockedCards.getIfPresent(storable.getIdentifier());
	}

	@Override
	public void update(LockedCard lockedCard) {
		throw new UnsupportedOperationException("A locked card can't be modified");
	}

	@Override
	public void delete(Storable storable) {
		lockedCards.invalidate(storable.getIdentifier());
	}

	@Override
	public List<LockedCard> list() {
		final Map<String, LockedCard> lockedCardsMap = lockedCards.asMap();
		return Lists.newArrayList(lockedCardsMap.values());
	}

}
