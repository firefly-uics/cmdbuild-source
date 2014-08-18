package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.lookup.LookupLogic.LookupTypeQuery;
import org.cmdbuild.service.rest.LookupTypes;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.LookupTypeDetail;
import org.cmdbuild.service.rest.dto.LookupTypeListResponse;
import org.cmdbuild.service.rest.dto.LookupTypeResponse;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.cmdbuild.service.rest.serialization.ToLookupTypeDetail;

import com.google.common.base.Predicate;

public class CxfLookupTypes extends CxfService implements LookupTypes {

	private static final ToLookupTypeDetail TO_LOOKUP_TYPE_DETAIL = ToLookupTypeDetail.newInstance().build();

	@Override
	public SimpleResponse<LookupTypeDetail> read(final String type) {
		final PagedElements<LookupType> lookupTypes = lookupLogic().getAllTypes(new LookupTypeQuery() {

			@Override
			public Integer limit() {
				return null;
			}

			@Override
			public Integer offset() {
				return null;
			}

		});

		final LookupTypeDetail elements = from(lookupTypes) //
				.filter(new Predicate<LookupType>() {

					@Override
					public boolean apply(final LookupType input) {
						return input.name.equals(type);
					}

				}) //
				.transform(TO_LOOKUP_TYPE_DETAIL) //
				.first() //
				.get();
		return LookupTypeResponse.newInstance() //
				.withElement(elements) //
				.build();
	}

	@Override
	public ListResponse<LookupTypeDetail> readAll(final Integer limit, final Integer offset) {
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
		return LookupTypeListResponse.newInstance() //
				.withElements(elements) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(lookupTypes.totalSize()) //
						.build()) //
				.build();
	}

}