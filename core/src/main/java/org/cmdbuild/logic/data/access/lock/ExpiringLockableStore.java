package org.cmdbuild.logic.data.access.lock;

import static com.google.common.base.Optional.fromNullable;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import org.cmdbuild.logic.data.access.lock.LockableStore.Lock;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class ExpiringLockableStore<M extends Lock> implements LockableStore<M> {

	public static interface Configuration {

		long getExpirationTimeInMilliseconds();

	}

	private final Cache<Lockable, M> cache;

	public ExpiringLockableStore(final Configuration configuration) {
		this.cache = CacheBuilder.newBuilder() //
				.expireAfterWrite(configuration.getExpirationTimeInMilliseconds(), MILLISECONDS) //
				.build();
	}

	@Override
	public void add(final Lockable lockable, final M lock) {
		cache.put(lockable, lock);
	}

	@Override
	public void remove(final Lockable lockable) {
		cache.invalidate(lockable);
	}

	@Override
	public boolean isPresent(final Lockable lockable) {
		return get(lockable).isPresent();
	}

	@Override
	public Optional<M> get(final Lockable lockable) {
		return fromNullable(cache.getIfPresent(lockable));
	}

	@Override
	public Iterable<Lockable> stored() {
		return cache.asMap().keySet();
	}

	@Override
	public void removeAll() {
		cache.invalidateAll();
	}

}
