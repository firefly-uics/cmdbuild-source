package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.service.rest.LookupTypes;
import org.cmdbuild.service.rest.dto.LookupDetail;
import org.cmdbuild.service.rest.dto.LookupDetailResponse;
import org.cmdbuild.service.rest.dto.LookupTypeDetail;
import org.cmdbuild.service.rest.dto.LookupTypeDetailResponse;
import org.cmdbuild.service.rest.serialization.ToLookupDetail;
import org.cmdbuild.service.rest.serialization.ToLookupTypeDetail;

import com.google.common.collect.Lists;

public class CxfLookupTypes extends CxfService implements LookupTypes {

	private static final ToLookupTypeDetail TO_LOOKUP_TYPE_DETAIL = ToLookupTypeDetail.newInstance().build();
	private static final ToLookupDetail TO_LOOKUP_DETAIL = ToLookupDetail.newInstance().build();

	@Override
	public LookupTypeDetailResponse getLookupTypes() {
		final Iterable<? extends LookupType> lookupTypes = Lists.newArrayList(lookupLogic().getAllTypes());

		final Iterable<LookupTypeDetail> details = from(lookupTypes) //
				.transform(TO_LOOKUP_TYPE_DETAIL);
		return LookupTypeDetailResponse.newInstance() //
				.withDetails(details) //
				.withTotal(size(details)) //
				.build();
	}

	@Override
	public LookupDetailResponse getLookups(final String type, final boolean activeOnly) {
		final LookupType lookupType = LookupType.newInstance().withName(type).build();
		final Iterable<? extends Lookup> lookups = lookupLogic().getAllLookup(lookupType, activeOnly);

		final Iterable<LookupDetail> details = from(lookups) //
				.transform(TO_LOOKUP_DETAIL);
		return LookupDetailResponse.newInstance() //
				.withDetails(details) //
				.withTotal(size(details)) //
				.build();
	}

}
