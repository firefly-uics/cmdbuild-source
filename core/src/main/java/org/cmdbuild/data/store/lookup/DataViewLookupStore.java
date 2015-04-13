package org.cmdbuild.data.store.lookup;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.reflect.Reflection.newProxy;
import static org.cmdbuild.common.utils.Reflection.unsupported;
import static org.cmdbuild.data.store.lookup.Functions.toLookupType;
import static org.cmdbuild.data.store.lookup.Predicates.lookupWithType;

import java.util.Map;

import org.cmdbuild.data.store.ForwardingStore;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DataViewLookupStore extends ForwardingStore<Lookup> implements LookupStore {

	protected static final Marker marker = MarkerFactory.getMarker(DataViewLookupStore.class.getName());

	@SuppressWarnings("unchecked")
	private static final Store<Lookup> unsupported = newProxy(Store.class, unsupported("method not supported"));

	private final Store<Lookup> delegate;

	public DataViewLookupStore(final Store<Lookup> delegate) {
		this.delegate = delegate;
	}

	@Override
	protected Store<Lookup> delegate() {
		return delegate;
	}

	@Override
	public void delete(final Storable storable) {
		unsupported.delete(storable);
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
