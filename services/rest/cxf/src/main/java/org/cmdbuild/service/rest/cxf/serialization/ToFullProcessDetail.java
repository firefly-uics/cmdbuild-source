package org.cmdbuild.service.rest.cxf.serialization;

import static org.cmdbuild.service.rest.model.Models.newProcessWithFullDetails;
import static com.google.common.collect.FluentIterable.*;
import static org.cmdbuild.data.store.lookup.Functions.*;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.service.rest.model.ProcessWithFullDetails;
import org.cmdbuild.workflow.LookupHelper;

import com.google.common.base.Function;

public class ToFullProcessDetail implements Function<CMClass, ProcessWithFullDetails> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToFullProcessDetail> {

		private LookupHelper lookupHelper;

		private Builder() {
			// use static method
		}

		public Builder withLookupHelper(final LookupHelper lookupHelper) {
			this.lookupHelper = lookupHelper;
			return this;
		}

		@Override
		public ToFullProcessDetail build() {
			return new ToFullProcessDetail(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final LookupHelper lookupHelper;

	private ToFullProcessDetail(final Builder builder) {
		this.lookupHelper = builder.lookupHelper;
	}

	@Override
	public ProcessWithFullDetails apply(final CMClass input) {
		final CMClass parent = input.getParent();
		return newProcessWithFullDetails() //
				.withId(input.getName()) //
				.withName(input.getName()) //
				.withDescription(input.getDescription()) //
				.thatIsPrototype(input.isSuperclass()) //
				.withDescriptionAttributeName(input.getDescriptionAttributeName()) //
				.withStatuses(from(lookupHelper.allLookups()).transform(toLookupId())) //
				.withParent((parent == null) ? null : parent.getName()) //
				.build();
	}

}
