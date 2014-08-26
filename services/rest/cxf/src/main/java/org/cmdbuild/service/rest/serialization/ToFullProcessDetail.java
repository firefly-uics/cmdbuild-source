package org.cmdbuild.service.rest.serialization;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.service.rest.dto.FullProcessDetail;

import com.google.common.base.Function;

public class ToFullProcessDetail implements Function<CMClass, FullProcessDetail> {

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
	public FullProcessDetail apply(final CMClass input) {
		final CMClass parent = input.getParent();
		return FullProcessDetail.newInstance() //
				.withName(input.getName()) //
				.withDescription(input.getDescription()) //
				.thatIsPrototype(input.isSuperclass()) //
				.withDescriptionAttributeName(input.getDescriptionAttributeName()) //
				.withParent((parent == null) ? null : parent.getName()) //
				.build();
	}

}
