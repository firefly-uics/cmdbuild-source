package org.cmdbuild.service.rest.cxf.serialization;

import static org.cmdbuild.service.rest.model.Models.newProcessStatus;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.service.rest.model.ProcessStatus;

import com.google.common.base.Function;

public class ToProcessStatus implements Function<Lookup, ProcessStatus> {

	@Override
	public ProcessStatus apply(final Lookup input) {
		return newProcessStatus() //
				.withId(input.getId()) //
				.withDescription(input.getDescription()) //
				.build();
	}

}
