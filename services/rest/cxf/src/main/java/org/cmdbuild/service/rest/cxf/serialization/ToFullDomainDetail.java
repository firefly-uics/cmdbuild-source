package org.cmdbuild.service.rest.cxf.serialization;

import static org.cmdbuild.service.rest.model.Builders.newDomainWithFullDetails;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.service.rest.model.DomainWithFullDetails;

import com.google.common.base.Function;

public class ToFullDomainDetail implements Function<CMDomain, DomainWithFullDetails> {

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
	public DomainWithFullDetails apply(final CMDomain input) {
		return newDomainWithFullDetails() //
				.withId(input.getId()) //
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
