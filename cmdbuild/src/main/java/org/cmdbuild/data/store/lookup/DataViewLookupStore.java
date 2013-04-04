package org.cmdbuild.data.store.lookup;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.DataViewStore;

import com.google.common.base.Predicate;

public class DataViewLookupStore extends DataViewStore<LookupDto> implements LookupStore {

	public DataViewLookupStore(final CMDataView view, final StorableConverter<LookupDto> converter) {
		super(view, converter);
	}

	@Override
	public void delete(final Storable storable) {
		throw new UnsupportedOperationException();
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
