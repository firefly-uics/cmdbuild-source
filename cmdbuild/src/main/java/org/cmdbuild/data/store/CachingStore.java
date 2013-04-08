package org.cmdbuild.data.store;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newConcurrentMap;

import java.util.List;
import java.util.Map;

import org.cmdbuild.data.store.Store.Storable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class CachingStore<T extends Storable> extends ForwardingStore<T> {

	private static final Marker marker = MarkerFactory.getMarker(DataViewStore.class.getName());

	private static class Cache<T extends Storable> {

		private final Map<String, T> cache;

		public Cache(final Store<T> inner) {
			logger.info(marker, "initializing cache");
			cache = newConcurrentMap();
			for (final T storable : inner.list()) {
				add(storable);
			}
		}

		public T get(final Storable storable) {
			return cache.get(storable.getIdentifier());
		}

		public void add(final T storable) {
			cache.put(storable.getIdentifier(), storable);
		}

		public void remove(final Storable storable) {
			cache.remove(storable.getIdentifier());
		}

		public List<T> values() {
			return newArrayList(cache.values());
		}

	}

	private final Cache<T> cache;

	public CachingStore(final Store<T> inner) {
		super(inner);
		this.cache = new Cache<T>(inner);
	}

	@Override
	public Storable create(final T storable) {
		final Storable created = super.create(storable);
		final T readed = super.read(created);
		cache.add(readed);
		return created;
	}

	@Override
	public T read(final Storable storable) {
		return cache.get(storable);
	}

	@Override
	public void update(final T storable) {
		super.update(storable);
		cache.add(storable);
	}

	@Override
	public void delete(final Storable storable) {
		super.delete(storable);
		cache.remove(storable);
	}

	@Override
	public List<T> list() {
		return cache.values();
	}

}
