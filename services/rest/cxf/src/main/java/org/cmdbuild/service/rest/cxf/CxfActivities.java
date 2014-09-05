package org.cmdbuild.service.rest.cxf;

import org.cmdbuild.service.rest.Activities;
import org.cmdbuild.service.rest.ProcessInstanceActivities;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.ProcessActivity;
import org.cmdbuild.service.rest.dto.ProcessActivityDefinition;
import org.cmdbuild.service.rest.dto.SimpleResponse;

public class CxfActivities implements Activities {

	private final ProcessInstanceActivities delegate;

	public CxfActivities(final ProcessInstanceActivities delegate) {
		this.delegate = delegate;
	}

	@Override
	public ListResponse<ProcessActivity> read(final String type, final Long instance) {
		return delegate.read(type, instance);
	}

	@Override
	public SimpleResponse<ProcessActivityDefinition> read(final String activity, final String type, final Long instance) {
		return delegate.read(type, instance, activity);
	}

}
