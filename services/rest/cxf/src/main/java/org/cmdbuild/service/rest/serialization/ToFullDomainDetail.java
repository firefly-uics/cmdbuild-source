package org.cmdbuild.service.rest.serialization;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.service.rest.dto.FullDomainDetail;

import com.google.common.base.Function;

public class ToFullDomainDetail implements Function<CMDomain, FullDomainDetail> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToFullDomainDetail> {

		private Builder() {
			// use static method
		}

		@Override
		public ToFullDomainDetail build() {
			return new ToFullDomainDetail(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private ToFullDomainDetail(final Builder builder) {
		// nothing to do
	}

	@Override
	public FullDomainDetail apply(final CMDomain input) {
		return FullDomainDetail.newInstance() //
				.withName(input.getName()) //
				.withDescription(input.getDescription()) //
				.withClassSource(input.getClass1().getName()) //
				.withClassDestination(input.getClass2().getName()) //
				.withCardinality(input.getCardinality()) //
				.withDescriptionDirect(input.getDescription1()) //
				.withDescriptionInverse(input.getDescription2()) //
				.withDescriptionMasterDetail(input.getMasterDetailDescription()) //
				.build();
	}

}
