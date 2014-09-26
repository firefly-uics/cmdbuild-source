package org.cmdbuild.service.rest.cxf;

import org.cmdbuild.service.rest.Activities;
import org.cmdbuild.service.rest.ProcessInstanceActivities;
import org.cmdbuild.service.rest.dto.ProcessActivityWithBasicDetails;
import org.cmdbuild.service.rest.dto.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.dto.ResponseMultiple;
import org.cmdbuild.service.rest.dto.ResponseSingle;

public class CxfActivities implements Activities {

	private final ProcessInstanceActivities delegate;

	public CxfActivities(final ProcessInstanceActivities delegate) {
		this.delegate = delegate;
	}

	@Override
	public ResponseMultiple<ProcessActivityWithBasicDetails> read(final String type, final Long instance) {
		return delegate.read(type, instance);
	}

	@Override
	public ResponseSingle<ProcessActivityWithFullDetails> read(final String activity, final String type,
			final Long instance) {
		return delegate.read(type, instance, activity);
	}

}
