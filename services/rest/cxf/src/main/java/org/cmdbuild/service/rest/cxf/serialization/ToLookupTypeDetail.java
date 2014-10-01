package org.cmdbuild.service.rest.cxf.serialization;

import static org.cmdbuild.service.rest.cxf.serialization.FakeId.fakeId;
import static org.cmdbuild.service.rest.model.Builders.newLookupTypeDetail;

import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.service.rest.model.LookupTypeDetail;

import com.google.common.base.Function;

public class ToLookupTypeDetail implements Function<LookupType, LookupTypeDetail> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToLookupTypeDetail> {

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
		return newLookupTypeDetail() //
				.withId(fakeId(input.name)) //
				.withName(input.name) //
				.withParent(fakeId(input.parent)) //
				.build();
	}

}
