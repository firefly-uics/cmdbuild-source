package org.cmdbuild.logic.data.access.lock;

import java.util.Date;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

public class DefaultLockManager implements LockManager {

	public static class Lock implements LockableStore.Lock {

		private final String user;
		private final Date time;

		public Lock(final String user, final Date time) {
			this.user = user;
			this.time = time;
		}

		public String getUser() {
			return user;
		}

		public Date getTime() {
			return time;
		}

	}

	public static class DurationExpired implements Predicate<Lock> {

		public static interface Configuration {

			long getExpirationTimeInMilliseconds();

		}

		private final Configuration configuration;

		public DurationExpired(final Configuration configuration) {
			this.configuration = configuration;
		}

		@Override
		public boolean apply(final Lock input) {
			final Date time = input.getTime();
			return (time == null) ? true : expired(time);
		}

		private boolean expired(final Date time) {
			final long now = new Date().getTime();
			return now > (time.getTime() + configuration.getExpirationTimeInMilliseconds());
		}
	}

	private final LockableStore<Lock> store;
	private final Supplier<String> usernameSupplier;

	public DefaultLockManager(final LockableStore<Lock> store, final Supplier<String> usernameSupplier) {
		this.usernameSupplier = usernameSupplier;
		this.store = store;
	}

	@Override
	public void lock(final Lockable lockable) throws LockedByAnotherUser {
		final Optional<Lock> lock = store.get(lockable);
		if (lock.isPresent() && !lock.get().getUser().equals(usernameSupplier.get())) {
			throw new LockedByAnotherUser(lock.get().getUser(), lock.get().getTime());
		}
		store.add(lockable, new Lock(usernameSupplier.get(), new Date()));
	}

	@Override
	public void unlock(final Lockable lockable) throws LockedByAnotherUser {
		final Optional<Lock> lock = store.get(lockable);
		if (!lock.isPresent()) {
			checkNotLockedAsParent(lockable);
		} else if (!lock.get().getUser().equals(usernameSupplier.get())) {
			throw new LockedByAnotherUser(lock.get().getUser(), lock.get().getTime());
		} else {
			store.remove(lockable);
		}
	}

	@Override
	public void unlockAll() {
		store.removeAll();
	}

	@Override
	public void checkNotLocked(final Lockable lockable) throws LockedByAnotherUser {
		final Optional<Lock> lock = store.get(lockable);
		if (lock.isPresent()) {
			throw new LockedByAnotherUser(lock.get().getUser(), lock.get().getTime());
		}
		checkNotLockedAsParent(lockable);
	}

	private void checkNotLockedAsParent(final Lockable lockable) throws LockedByAnotherUser {
		for (final Lockable element : store.stored()) {
			Optional<Lockable> parent = element.parent();
			while (parent.isPresent()) {
				final Lockable _lockable = parent.get();
				if (_lockable.equals(lockable)) {
					final Optional<Lock> _lock = store.get(element);
					throw new LockedByAnotherUser(_lock.get().getUser(), _lock.get().getTime());
				}
				parent = _lockable.parent();
			}
		}
	}

	@Override
	public void checkLockedByUser(final Lockable lockable, final String userName) throws ExpectedLocked,
			LockedByAnotherUser {
		final Optional<Lock> lock = store.get(lockable);
		if (!lock.isPresent()) {
			throw new ExpectedLocked();
		} else if (!lock.get().getUser().equals(userName)) {
			throw new LockedByAnotherUser(lock.get().getUser(), lock.get().getTime());
		}
	}

}
