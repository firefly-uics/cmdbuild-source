package org.cmdbuild.data.store.lookup;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newHashMap;
import static org.cmdbuild.data.store.lookup.Functions.toLookupType;
import static org.cmdbuild.data.store.lookup.Predicates.lookupWithType;

import java.util.Collection;
import java.util.Map;

import org.cmdbuild.data.store.Groupable;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DataViewLookupStore implements LookupStore {

	protected static final Marker marker = MarkerFactory.getMarker(DataViewLookupStore.class.getName());

	private final Store<Lookup> inner;

	public DataViewLookupStore(final Store<Lookup> store) {
		this.inner = store;
	}

	@Override
	public org.cmdbuild.data.store.Storable create(final Lookup storable) {
		return inner.create(storable);
	}

	@Override
	public Lookup read(final org.cmdbuild.data.store.Storable storable) {
		return inner.read(storable);
	}

	@Override
	public void update(final Lookup storable) {
		inner.update(storable);
	}

	@Override
	public void delete(final Storable storable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Lookup> readAll() {
		return inner.readAll();
	}

	@Override
	public Collection<Lookup> readAll(final Groupable groupable) {
		return inner.readAll(groupable);
	}

	@Override
	public Iterable<Lookup> readAll(final LookupType type) {
		logger.debug(marker, "getting lookups with type '{}'", type);

		final Iterable<Lookup> lookups = readAll();

		final Map<Long, Lookup> lookupsById = newHashMap();
		for (final Lookup lookup : lookups) {
			lookupsById.put(lookup.getId(), lookup);
		}

		for (final Lookup lookup : lookups) {
			final Lookup lookupWithParent = buildLookupWithParentLookup(lookup, lookupsById);
			lookupsById.put(lookupWithParent.getId(), lookupWithParent);
		}

		return from(lookupsById.values()) //
				.filter(lookupWithType(type));
	}

	private Lookup buildLookupWithParentLookup(final Lookup lookup, final Map<Long, Lookup> lookupsById) {
		final Lookup lookupWithParent;
		final Lookup parent = lookupsById.get(lookup.parentId());
		if (parent != null) {
			final Long grandparentId = parent.parentId();
			final Lookup parentWithGrandparent;
			if (grandparentId != null) {
				parentWithGrandparent = buildLookupWithParentLookup(parent, lookupsById);
			} else {
				parentWithGrandparent = parent;
			}
			lookupWithParent = LookupImpl.newInstance() //
					.clone(lookup) //
					.withParent(parentWithGrandparent) //
					.build();
		} else {
			lookupWithParent = lookup;
		}
		return lookupWithParent;
	}

	@Override
	public Iterable<LookupType> readAllTypes() {
		return from(readAll()) //
				.transform(toLookupType()) //
				.toSet();
	}

}
