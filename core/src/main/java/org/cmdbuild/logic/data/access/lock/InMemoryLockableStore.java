package org.cmdbuild.logic.data.access.lock;

import static com.google.common.base.Optional.fromNullable;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import org.cmdbuild.logic.data.access.lock.LockableStore.Metadata;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class InMemoryLockableStore<M extends Metadata> implements LockableStore<M> {

	public static interface Configuration {

		long getExpirationTimeInMilliseconds();

	}

	private final Cache<Lockable, M> cache;

	public InMemoryLockableStore(final Configuration configuration) {
		this.cache = CacheBuilder.newBuilder() //
				.expireAfterWrite(configuration.getExpirationTimeInMilliseconds(), MILLISECONDS) //
				.build();
	}

	@Override
	public void add(final Lockable lockable, final M metadata) {
		cache.put(lockable, metadata);
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
	public void removeAll() {
		cache.invalidateAll();
	}

}
