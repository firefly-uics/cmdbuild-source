package org.cmdbuild.logic.data.access.lock;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Date;

import org.cmdbuild.exception.ConsistencyException;
import org.cmdbuild.exception.ConsistencyException.ConsistencyExceptionType;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class InMemoryLockManager implements LockManager {

	public static interface Configuration {

		boolean isUsernameVisible();

		long getExpirationTimeInMilliseconds();

	}

	private static class LockableWithMetadata {

		private final Lockable lockable;
		private final String user;
		private final Date time;

		public LockableWithMetadata(final Lockable lockable, final String user, final Date time) {
			this.lockable = lockable;
			this.user = user;
			this.time = time;
		}

		public Lockable getLockable() {
			return lockable;
		}

		public String getUser() {
			return user;
		}

		public Date getTime() {
			return time;
		}

	}

	private final boolean displayLockerUsername;
	private final Supplier<String> usernameSupplier;
	private final Cache<Lockable, LockableWithMetadata> lockedCards;

	public InMemoryLockManager(final Configuration configuration, final Supplier<String> usernameSupplier) {
		this.displayLockerUsername = configuration.isUsernameVisible();
		this.usernameSupplier = usernameSupplier;
		this.lockedCards = CacheBuilder.newBuilder() //
				.expireAfterWrite(configuration.getExpirationTimeInMilliseconds(), MILLISECONDS) //
				.build();
	}

	@Override
	public void lock(final Lockable lockable) {
		final Optional<LockableWithMetadata> lockedCard = fromNullable(lockedCards.getIfPresent(lockable));
		if (lockedCard.isPresent() && !usernameSupplier.get().equals(lockedCard.get().getUser())) {
			throw createLockedCardException(lockedCard.get());
		}
		lockedCards.put(lockable, new LockableWithMetadata(lockable, usernameSupplier.get(), new Date()));
	}

	@Override
	public void unlock(final Lockable lockable) {
		final Optional<LockableWithMetadata> lockedCard = fromNullable(lockedCards.getIfPresent(lockable));
		if (!lockedCard.isPresent()) {
			return;
		} else if (!lockedCard.get().getUser().equals(usernameSupplier.get())) {
			throw createLockedCardException(lockedCard.get());
		} else {
			lockedCards.invalidate(lockable);
		}
	}

	@Override
	public void unlockAll() {
		for (final LockableWithMetadata element : newArrayList(lockedCards.asMap().values())) {
			lockedCards.invalidate(element.getLockable());
		}
	}

	@Override
	public void checkNotLocked(final Lockable lockable) {
		final Optional<LockableWithMetadata> lockedCard = fromNullable(lockedCards.getIfPresent(lockable));
		if (lockedCard.isPresent()) {
			throw createLockedCardException(lockedCard.get());
		}
	}

	@Override
	public void checkLockedbyUser(final Lockable lockable, final String userName) {
		final Optional<LockableWithMetadata> lockedCard = fromNullable(lockedCards.getIfPresent(lockable));
		if (lockedCard.isPresent() && !lockedCard.get().getUser().equals(userName)) {
			throw createLockedCardException(lockedCard.get());
		}
	}

	private ConsistencyException createLockedCardException(final LockableWithMetadata element) {
		final long currentTimestamp = new Date().getTime();
		final long differenceInMilliseconds = currentTimestamp - element.getTime().getTime();
		final long differenceInSeconds = differenceInMilliseconds / 1000;
		return ConsistencyExceptionType.LOCKED_CARD //
				.createException(displayLockerUsername ? element.getUser() : "undefined", "" + differenceInSeconds);
	}

}
