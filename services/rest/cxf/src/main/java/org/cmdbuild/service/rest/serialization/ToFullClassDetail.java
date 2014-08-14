package org.cmdbuild.service.rest.serialization;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.service.rest.dto.FullClassDetail;

import com.google.common.base.Function;

public class ToFullClassDetail implements Function<CMClass, FullClassDetail> {

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
	public FullClassDetail apply(final CMClass input) {
		return FullClassDetail.newInstance() //
				.withName(input.getName()) //
				.withDescription(input.getDescription()) //
				.withSuperclassStatus(input.isSuperclass()) //
				.withDescriptionAttributeName(input.getDescriptionAttributeName()) //
				.withParent(input.getParent().getName()) //
				.build();
	}

}
