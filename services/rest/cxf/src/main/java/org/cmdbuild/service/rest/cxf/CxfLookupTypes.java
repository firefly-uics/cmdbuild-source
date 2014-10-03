package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.service.rest.cxf.serialization.FakeId.fakeId;
import static org.cmdbuild.service.rest.model.Builders.newMetadata;
import static org.cmdbuild.service.rest.model.Builders.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Builders.newResponseSingle;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic.LookupTypeQuery;
import org.cmdbuild.service.rest.LookupTypes;
import org.cmdbuild.service.rest.cxf.serialization.ToLookupTypeDetail;
import org.cmdbuild.service.rest.model.LookupTypeDetail;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class CxfLookupTypes implements LookupTypes {

	private static final ToLookupTypeDetail TO_LOOKUP_TYPE_DETAIL = ToLookupTypeDetail.newInstance().build();

	private static class FakeIdPredicate implements Predicate<LookupType> {

		private final Long fakeId;

		public FakeIdPredicate(final Long fakeId) {
			this.fakeId = fakeId;
		}

		@Override
		public boolean apply(final LookupType input) {
			return fakeId(input.name).equals(fakeId);
		}

	}

	private final ErrorHandler errorHandler;
	private final LookupLogic lookupLogic;

	public CxfLookupTypes(final ErrorHandler errorHandler, final LookupLogic lookupLogic) {
		this.errorHandler = errorHandler;
		this.lookupLogic = lookupLogic;
	}

	@Override
	public ResponseSingle<LookupTypeDetail> read(final Long lookupTypeId) {
		final Optional<LookupType> element = lookupLogic.typeFor(new FakeIdPredicate(lookupTypeId));
		if (!element.isPresent()) {
			errorHandler.lookupTypeNotFound(lookupTypeId);
		}
		return newResponseSingle(LookupTypeDetail.class) //
				.withElement(TO_LOOKUP_TYPE_DETAIL.apply(element.get())) //
				.build();
	}

	@Override
	public ResponseMultiple<LookupTypeDetail> readAll(final Integer limit, final Integer offset) {
		final PagedElements<LookupType> lookupTypes = lookupLogic.getAllTypes(new LookupTypeQuery() {

			@Override
			public Integer limit() {
				return limit;
			}

			@Override
			public Integer offset() {
				return offset;
			}

		});

		final Iterable<LookupTypeDetail> elements = from(lookupTypes) //
				.transform(TO_LOOKUP_TYPE_DETAIL);
		return newResponseMultiple(LookupTypeDetail.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(lookupTypes.totalSize())) //
						.build()) //
				.build();
	}

}
