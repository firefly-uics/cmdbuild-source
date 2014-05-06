package org.cmdbuild.service.rest.serialization;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.service.rest.dto.LookupDetail;

import com.google.common.base.Function;

public class ToLookupDetail implements Function<Lookup, LookupDetail> {

	public static class Builder implements org.cmdbuild.common.Builder<ToLookupDetail> {

		private Builder() {
			// use static method
		}

		@Override
		public ToLookupDetail build() {
			return new ToLookupDetail(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private ToLookupDetail(final Builder builder) {
		// nothing to do
	}

	@Override
	public LookupDetail apply(final Lookup lookup) {		
		return LookupDetail.newInstance() //
				.withId(lookup.getId()) //
				.withCode(lookup.code) //
				.withDescription(lookup.description) //
				.withType(lookup.type.name) //
				.withNumber(lookup.number) //
				.thatIsActive(lookup.active) //
				.thatIsDefault(lookup.isDefault) //
				.withParentId(lookup.parentId) //
				.withParentType(lookup.type.parent) //
				.build();
	}

}
