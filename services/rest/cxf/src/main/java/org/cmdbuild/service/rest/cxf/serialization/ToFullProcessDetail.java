package org.cmdbuild.service.rest.cxf.serialization;

import static org.cmdbuild.service.rest.model.Builders.newProcessWithFullDetails;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.service.rest.model.ProcessWithFullDetails;

import com.google.common.base.Function;

public class ToFullProcessDetail implements Function<CMClass, ProcessWithFullDetails> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToFullProcessDetail> {

		private Builder() {
			// use static method
		}

		@Override
		public ToFullProcessDetail build() {
			return new ToFullProcessDetail(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private ToFullProcessDetail(final Builder builder) {
		// nothing to do
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
				.withParent((parent == null) ? null : parent.getName()) //
				.build();
	}

}
