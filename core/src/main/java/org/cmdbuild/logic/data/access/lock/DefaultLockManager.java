package org.cmdbuild.logic.data.access.lock;

import java.util.Date;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

public class DefaultLockManager implements LockManager {

	public static class Metadata implements LockableStore.Metadata {

		private final Lockable lockable;
		private final String user;
		private final Date time;

		public Metadata(final Lockable lockable, final String user, final Date time) {
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

	private final Supplier<String> usernameSupplier;
	private final LockableStore<Metadata> store;

	public DefaultLockManager(final Supplier<String> usernameSupplier, final LockableStore<Metadata> store) {
		this.usernameSupplier = usernameSupplier;
		this.store = store;
	}

	@Override
	public void lock(final Lockable lockable) throws LockedByAnotherUser {
		final Optional<Metadata> metadata = store.get(lockable);
		if (metadata.isPresent() && !usernameSupplier.get().equals(metadata.get().getUser())) {
			throw new LockedByAnotherUser(metadata.get().getUser(), metadata.get().getTime());
		}
		store.add(lockable, new Metadata(lockable, usernameSupplier.get(), new Date()));
	}

	@Override
	public void unlock(final Lockable lockable) throws LockedByAnotherUser {
		final Optional<Metadata> metadata = store.get(lockable);
		if (!metadata.isPresent()) {
			return;
		} else if (!metadata.get().getUser().equals(usernameSupplier.get())) {
			throw new LockedByAnotherUser(metadata.get().getUser(), metadata.get().getTime());
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
		final Optional<Metadata> metadata = store.get(lockable);
		if (metadata.isPresent()) {
			throw new LockedByAnotherUser(metadata.get().getUser(), metadata.get().getTime());
		}
	}

	@Override
	public void checkLockedbyUser(final Lockable lockable, final String userName) throws LockedByAnotherUser {
		final Optional<Metadata> metadata = store.get(lockable);
		if (metadata.isPresent() && !metadata.get().getUser().equals(userName)) {
			throw new LockedByAnotherUser(metadata.get().getUser(), metadata.get().getTime());
		}
	}

}
