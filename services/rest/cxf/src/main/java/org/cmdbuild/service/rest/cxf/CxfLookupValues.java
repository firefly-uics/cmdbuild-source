package org.cmdbuild.service.rest.cxf;

import org.cmdbuild.service.rest.LookupTypeValues;
import org.cmdbuild.service.rest.LookupValues;
import org.cmdbuild.service.rest.dto.LookupDetail;
import org.cmdbuild.service.rest.dto.ResponseMultiple;
import org.cmdbuild.service.rest.dto.ResponseSingle;

public class CxfLookupValues implements LookupValues {

	private final LookupTypeValues delegate;

	public CxfLookupValues(final LookupTypeValues delegate) {
		this.delegate = delegate;
	}

	@Override
	public ResponseSingle<LookupDetail> read(final String type, final Long id) {
		return delegate.read(type, id);
	}

	@Override
	public ResponseMultiple<LookupDetail> readAll(final String type, final boolean activeOnly, final Integer limit,
			final Integer offset) {
		return delegate.readAll(type, activeOnly, limit, offset);
	}

}
