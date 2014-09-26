package org.cmdbuild.service.rest.cxf.serialization;

import static org.cmdbuild.service.rest.dto.Builders.newClassWithFullDetails;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.service.rest.dto.ClassWithFullDetails;

import com.google.common.base.Function;

public class ToFullClassDetail implements Function<CMClass, ClassWithFullDetails> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToFullClassDetail> {

		private Builder() {
			// use static method
		}

		@Override
		public ToFullClassDetail build() {
			return new ToFullClassDetail(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private ToFullClassDetail(final Builder builder) {
		// nothing to do
	}

	@Override
	public ClassWithFullDetails apply(final CMClass input) {
		final CMClass parent = input.getParent();
		return newClassWithFullDetails() //
				.withId(input.getId()) //
				.withName(input.getName()) //
				.withDescription(input.getDescription()) //
				.thatIsPrototype(input.isSuperclass()) //
				.withDescriptionAttributeName(input.getDescriptionAttributeName()) //
				.withParent((parent == null) ? null : parent.getName()) //
				.build();
	}

}
