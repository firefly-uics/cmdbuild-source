package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.Iterables.size;
import static org.cmdbuild.service.rest.model.Models.newMetadata;
import static org.cmdbuild.service.rest.model.Models.newResponseMultiple;

import org.cmdbuild.service.rest.ProcessesConfiguration;
import org.cmdbuild.service.rest.model.ProcessStatus;
import org.cmdbuild.service.rest.model.ResponseMultiple;

public class CxfProcessesConfiguration implements ProcessesConfiguration {

	private final ProcessStatusHelper processStatusHelper;

	public CxfProcessesConfiguration(final ProcessStatusHelper processStatusHelper) {
		this.processStatusHelper = processStatusHelper;
	}

	@Override
	public ResponseMultiple<ProcessStatus> readStatuses() {
		final Iterable<ProcessStatus> elements = processStatusHelper.allValues();
		return newResponseMultiple(ProcessStatus.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(size(elements))) //
						.build()) //
				.build();
	}

}
