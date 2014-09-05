package org.cmdbuild.service.rest.cxf;

import javax.ws.rs.core.MultivaluedMap;

import org.cmdbuild.service.rest.Instances;
import org.cmdbuild.service.rest.ProcessInstances;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.ProcessInstance;
import org.cmdbuild.service.rest.dto.SimpleResponse;

public class CxfInstances implements Instances {

	private final ProcessInstances delegate;

	public CxfInstances(final ProcessInstances delegate) {
		this.delegate = delegate;
	}

	@Override
	public SimpleResponse<Long> create(final MultivaluedMap<String, String> formParams, final String type,
			final boolean advance) {
		return delegate.create(type, formParams, advance);
	}

	@Override
	public org.cmdbuild.service.rest.dto.SimpleResponse<ProcessInstance> read(final Long id, final String type) {
		return delegate.read(type, id);
	};

	@Override
	public ListResponse<ProcessInstance> read(final String type, final Integer limit, final Integer offset) {
		return delegate.read(type, limit, offset);
	}

	@Override
	public void update(final Long id, final String type, final String activity, final boolean advance,
			final MultivaluedMap<String, String> formParams) {
		delegate.update(type, id, activity, advance, formParams);
	}

	@Override
	public void delete(final Long id, final String type) {
		delegate.delete(type, id);
	};

}
