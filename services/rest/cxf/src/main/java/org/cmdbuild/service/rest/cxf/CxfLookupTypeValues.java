package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.logic.data.lookup.LookupLogic.UNUSED_LOOKUP_TYPE_QUERY;
import static org.cmdbuild.service.rest.cxf.serialization.FakeId.fakeId;
import static org.cmdbuild.service.rest.model.Builders.newMetadata;
import static org.cmdbuild.service.rest.model.Builders.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Builders.newResponseSingle;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic.LookupQuery;
import org.cmdbuild.service.rest.LookupTypeValues;
import org.cmdbuild.service.rest.cxf.serialization.ToLookupDetail;
import org.cmdbuild.service.rest.model.LookupDetail;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class CxfLookupTypeValues implements LookupTypeValues {

	private static final ToLookupDetail TO_LOOKUP_DETAIL = ToLookupDetail.newInstance().build();

	private final ErrorHandler errorHandler;
	private final LookupLogic lookupLogic;

	public CxfLookupTypeValues(final ErrorHandler errorHandler, final LookupLogic lookupLogic) {
		this.errorHandler = errorHandler;
		this.lookupLogic = lookupLogic;
	}

	@Override
	public ResponseSingle<LookupDetail> read(final Long lookupTypeId, final Long lookupValueId) {
		final Lookup lookup = lookupLogic.getLookup(lookupValueId);
		final LookupDetail element = TO_LOOKUP_DETAIL.apply(lookup);
		return newResponseSingle(LookupDetail.class) //
				.withElement(element) //
				.build();
	}

	@Override
	public ResponseMultiple<LookupDetail> readAll(final Long lookupTypeId, final boolean activeOnly,
			final Integer limit, final Integer offset) {
		final Optional<LookupType> found = from(lookupLogic.getAllTypes(UNUSED_LOOKUP_TYPE_QUERY)) //
				.filter(new Predicate<LookupType>() {

					@Override
					public boolean apply(final LookupType input) {
						return lookupTypeId.equals(fakeId(input.name));
					}

				}) //
				.first();
		if (!found.isPresent()) {
			errorHandler.lookupTypeNotFound(lookupTypeId);
		}
		final LookupType lookupType = LookupType.newInstance().withName(found.get().name).build();
		final PagedElements<Lookup> lookups = lookupLogic.getAllLookup(lookupType, activeOnly, new LookupQuery() {

			@Override
			public Integer limit() {
				return limit;
			}

			@Override
			public Integer offset() {
				return offset;
			}

		});

		final Iterable<LookupDetail> elements = from(lookups) //
				.transform(TO_LOOKUP_DETAIL);
		return newResponseMultiple(LookupDetail.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(lookups.totalSize())) //
						.build()) //
				.build();
	}
}
