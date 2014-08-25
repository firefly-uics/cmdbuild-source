package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic.LookupQuery;
import org.cmdbuild.service.rest.LookupTypeValues;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.LookupDetail;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.cmdbuild.service.rest.serialization.ToLookupDetail;

public class CxfLookupTypeValues implements LookupTypeValues {

	private static final ToLookupDetail TO_LOOKUP_DETAIL = ToLookupDetail.newInstance().build();

	private final LookupLogic lookupLogic;

	public CxfLookupTypeValues(final LookupLogic lookupLogic) {
		this.lookupLogic = lookupLogic;
	}

	@Override
	public SimpleResponse<LookupDetail> read(final String type, final Long id) {
		final Lookup lookup = lookupLogic.getLookup(id);
		final LookupDetail element = TO_LOOKUP_DETAIL.apply(lookup);
		return SimpleResponse.<LookupDetail> newInstance() //
				.withElement(element) //
				.build();
	}

	@Override
	public ListResponse<LookupDetail> readAll(final String type, final boolean activeOnly, final Integer limit,
			final Integer offset) {
		final LookupType lookupType = LookupType.newInstance().withName(type).build();
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
		return ListResponse.<LookupDetail> newInstance() //
				.withElements(elements) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(Long.valueOf(lookups.totalSize())) //
						.build()) //
				.build();
	}

}
