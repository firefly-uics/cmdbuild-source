package org.cmdbuild.data.store.lookup;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newHashMap;

import java.util.List;
import java.util.Map;

import org.cmdbuild.data.store.Store;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Predicate;

public class DataViewLookupStore implements LookupStore {

	protected static final Marker marker = MarkerFactory.getMarker(DataViewLookupStore.class.getName());

	private final Store<LookupDto> inner;

	public DataViewLookupStore(final Store<LookupDto> store) {
		this.inner = store;
	}

	@Override
	public org.cmdbuild.data.store.Store.Storable create(LookupDto storable) {
		return inner.create(storable);
	}

	@Override
	public LookupDto read(org.cmdbuild.data.store.Store.Storable storable) {
		return inner.read(storable);
	}

	@Override
	public void update(LookupDto storable) {
		inner.update(storable);
	}

	@Override
	public void delete(final Storable storable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<LookupDto> list() {
		return inner.list();
	}

	@Override
	public Iterable<LookupDto> listForType(final LookupTypeDto type) {
		logger.debug(marker, "getting lookups with type '{}'", type);

		final Iterable<LookupDto> lookups = list();

		final Map<Long, LookupDto> lookupsById = newHashMap();
		for (final LookupDto lookup : lookups) {
			lookupsById.put(lookup.id, lookup);
		}

		for (final LookupDto lookup : lookups) {
			final LookupDto lookupWithParent = buildLookupWithParentLookup(lookup, lookupsById);
			lookupsById.put(lookupWithParent.id, lookupWithParent);
		}

		return from(lookupsById.values()) //
				.filter(withType(type));
	}

	private LookupDto buildLookupWithParentLookup(final LookupDto lookup, final Map<Long, LookupDto> lookupsById) {
		final LookupDto lookupWithParent;
		final LookupDto parent = lookupsById.get(lookup.parentId);
		if (parent != null) {
			final Long grandparentId = parent.parentId;
			final LookupDto parentWithGrandparent;
			if (grandparentId != null) {
				parentWithGrandparent = buildLookupWithParentLookup(parent, lookupsById);
			} else {
				parentWithGrandparent = parent;
			}
			lookupWithParent = LookupDto.newInstance() //
					.clone(lookup) //
					.withParent(parentWithGrandparent) //
					.build();
		} else {
			lookupWithParent = lookup;
		}
		return lookupWithParent;
	}

	public static Predicate<LookupDto> withType(final LookupTypeDto type) {
		logger.debug("filtering lookups with type '{}'", type);
		return new Predicate<LookupDto>() {

			@Override
			public boolean apply(final LookupDto input) {
				return input.type.equals(type);
			}

		};
	}

}
