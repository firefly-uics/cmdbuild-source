package org.cmdbuild.logic.data.access.lock;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import org.cmdbuild.logic.data.access.lock.LockableStore.Lock;

import com.google.common.base.Optional;

public class InMemoryLockableStore<M extends Lock> implements LockableStore<M> {

	private final Map<Lockable, M> map;

	public InMemoryLockableStore() {
		this.map = newHashMap();
	}

	@Override
	public void add(final Lockable lockable, final M lock) {
		map.put(lockable, lock);
	}

	@Override
	public void remove(final Lockable lockable) {
		map.remove(lockable);
	}

	@Override
	public boolean isPresent(final Lockable lockable) {
		return get(lockable).isPresent();
	}

	@Override
	public Optional<M> get(final Lockable lockable) {
		return fromNullable(map.get(lockable));
	}

	@Override
	public Iterable<Lockable> stored() {
		return newArrayList(map.keySet());
	}

	@Override
	public void removeAll() {
		map.clear();
	}

}
