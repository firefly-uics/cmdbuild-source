package org.cmdbuild.service.rest.serialization;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.service.rest.dto.SimpleClassDetail;

import com.google.common.base.Function;

public class ToSimpleClassDetail implements Function<CMClass, SimpleClassDetail> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToSimpleClassDetail> {

		private Builder() {
			// use static method
		}

		@Override
		public ToSimpleClassDetail build() {
			return new ToSimpleClassDetail(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static final String MISSING_PARENT = null;

	private ToSimpleClassDetail(final Builder builder) {
		// nothing to do
	}

	@Override
	public SimpleClassDetail apply(final CMClass input) {
		final CMClass parent = input.getParent();
		return SimpleClassDetail.newInstance() //
				.withName(input.getName()) //
				.withDescription(input.getDescription()) //
				.withParent((parent == null) ? MISSING_PARENT : parent.getName()) //
				.build();
	}

}
