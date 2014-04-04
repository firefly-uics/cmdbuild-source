package org.cmdbuild.service.rest.serialization;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.service.rest.dto.SimpleClassDetail;

import com.google.common.base.Function;

public class ToSimpleClassDetail implements Function<CMClass, SimpleClassDetail> {

	public static class Builder implements org.cmdbuild.common.Builder<ToSimpleClassDetail> {

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

	private ToSimpleClassDetail(final Builder builder) {
		// nothing to do
	}

	@Override
	public SimpleClassDetail apply(final CMClass input) {
		return SimpleClassDetail.newInstance() //
				.withName(input.getName()) //
				.withDescription(input.getDescription()) //
				.build();
	}

}
