package org.cmdbuild.service.rest.cxf;

import org.cmdbuild.service.rest.LookupTypeValues;
import org.cmdbuild.service.rest.LookupValues;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.LookupDetail;
import org.cmdbuild.service.rest.dto.SimpleResponse;

public class CxfLookupValues implements LookupValues {

	private final LookupTypeValues delegate;

	public CxfLookupValues(final LookupTypeValues delegate) {
		this.delegate = delegate;
	}

	@Override
	public SimpleResponse<LookupDetail> read(final String type, final Long id) {
		return delegate.read(type, id);
	}

	@Override
	public ListResponse<LookupDetail> readAll(final String type, final boolean activeOnly, final Integer limit,
			final Integer offset) {
		return delegate.readAll(type, activeOnly, limit, offset);
	}

}
