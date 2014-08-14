package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.lookup.LookupLogic.LookupQuery;
import org.cmdbuild.service.rest.Lookups;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.LookupDetail;
import org.cmdbuild.service.rest.dto.LookupDetailResponse;
import org.cmdbuild.service.rest.dto.LookupResponse;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.cmdbuild.service.rest.serialization.ToLookupDetail;

public class CxfLookups extends CxfService implements Lookups {

	private static final ToLookupDetail TO_LOOKUP_DETAIL = ToLookupDetail.newInstance().build();

	@Override
	public SimpleResponse<LookupDetail> read(final String type, final Long id) {
		final Lookup lookup = lookupLogic().getLookup(id);
		final LookupDetail element = TO_LOOKUP_DETAIL.apply(lookup);
		return LookupResponse.newInstance() //
				.withElement(element) //
				.build();
	}

	@Override
	public ListResponse<LookupDetail> readAll(final String type, final boolean activeOnly, final Integer limit,
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
