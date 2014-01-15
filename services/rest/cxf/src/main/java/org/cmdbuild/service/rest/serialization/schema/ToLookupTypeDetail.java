package org.cmdbuild.service.rest.serialization.schema;

import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.service.rest.dto.schema.LookupTypeDetail;

import com.google.common.base.Function;

public class ToLookupTypeDetail implements Function<LookupType, LookupTypeDetail> {

	public static class Builder implements org.cmdbuild.common.Builder<ToLookupTypeDetail> {

		private Builder() {
			// use static method
		}

		@Override
		public ToLookupTypeDetail build() {
			return new ToLookupTypeDetail(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private ToLookupTypeDetail(final Builder builder) {
		// nothing to do
	}

	@Override
	public LookupTypeDetail apply(final LookupType input) {
		return LookupTypeDetail.newInstance() //
				.withName(input.name) //
				.withParent(input.parent) //
				.build();
	}

}
