package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static org.cmdbuild.service.rest.model.Models.newMetadata;
import static org.cmdbuild.service.rest.model.Models.newProcessStatus;
import static org.cmdbuild.service.rest.model.Models.newResponseMultiple;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.service.rest.ProcessesConfiguration;
import org.cmdbuild.service.rest.model.ProcessStatus;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.workflow.LookupHelper;

import com.google.common.base.Function;

public class CxfProcessesConfiguration implements ProcessesConfiguration {

	private static final Function<Lookup, ProcessStatus> TO_PROCESS_STATUS = new Function<Lookup, ProcessStatus>() {

		@Override
		public ProcessStatus apply(final Lookup input) {
			return newProcessStatus() //
					.withId(input.getId()) //
					.withDescription(input.getDescription()) //
					.build();
		}

	};

	private final LookupHelper lookupHelper;

	public CxfProcessesConfiguration(final LookupHelper lookupHelper) {
		this.lookupHelper = lookupHelper;
	}

	@Override
	public ResponseMultiple<ProcessStatus> readStatuses() {
		final Iterable<Lookup> allLookups = lookupHelper.allLookups();
		final Iterable<ProcessStatus> elements = from(allLookups).transform(TO_PROCESS_STATUS);
		return newResponseMultiple(ProcessStatus.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(size(elements))) //
						.build()) //
				.build();
	}

}
