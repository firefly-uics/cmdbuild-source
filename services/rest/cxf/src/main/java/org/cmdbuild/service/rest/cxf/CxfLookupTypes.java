package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.lookup.LookupLogic.LookupQuery;
import org.cmdbuild.logic.data.lookup.LookupLogic.LookupTypeQuery;
import org.cmdbuild.service.rest.LookupTypes;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.LookupDetail;
import org.cmdbuild.service.rest.dto.LookupDetailResponse;
import org.cmdbuild.service.rest.dto.LookupTypeDetail;
import org.cmdbuild.service.rest.dto.LookupTypeDetailResponse;
import org.cmdbuild.service.rest.serialization.ToLookupDetail;
import org.cmdbuild.service.rest.serialization.ToLookupTypeDetail;

public class CxfLookupTypes extends CxfService implements LookupTypes {

	private static final ToLookupTypeDetail TO_LOOKUP_TYPE_DETAIL = ToLookupTypeDetail.newInstance().build();
	private static final ToLookupDetail TO_LOOKUP_DETAIL = ToLookupDetail.newInstance().build();

	@Override
	public ListResponse<LookupTypeDetail> getLookupTypes(final Integer limit, final Integer offset) {
		final PagedElements<LookupType> lookupTypes = lookupLogic().getAllTypes(new LookupTypeQuery() {

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
		return LookupTypeDetailResponse.newInstance() //
				.withElements(elements) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(lookupTypes.totalSize()) //
						.build()) //
				.build();
	}

	@Override
	public ListResponse<LookupDetail> getLookups(final String type, final boolean activeOnly, final Integer limit,
			final Integer offset) {
		final LookupType lookupType = LookupType.newInstance().withName(type).build();
		final PagedElements<Lookup> lookups = lookupLogic().getAllLookup(lookupType, activeOnly, new LookupQuery() {

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
		return LookupDetailResponse.newInstance() //
				.withElements(elements) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(lookups.totalSize()) //
						.build()) //
				.build();
	}

}
