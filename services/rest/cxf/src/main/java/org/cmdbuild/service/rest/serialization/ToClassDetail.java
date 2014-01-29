package org.cmdbuild.service.rest.serialization;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.service.rest.dto.ClassDetail;

import com.google.common.base.Function;

public class ToClassDetail implements Function<CMClass, ClassDetail> {

	public static class Builder implements org.cmdbuild.common.Builder<ToClassDetail> {

		private Builder() {
			// use static method
		}

		@Override
		public ToClassDetail build() {
			return new ToClassDetail(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private ToClassDetail(final Builder builder) {
		// nothing to do
	}

	@Override
	public ClassDetail apply(final CMClass input) {
		return ClassDetail.newInstance() //
				.withName(input.getName()) //
				.withDescription(input.getDescription()) //
				.build();
	}

}
