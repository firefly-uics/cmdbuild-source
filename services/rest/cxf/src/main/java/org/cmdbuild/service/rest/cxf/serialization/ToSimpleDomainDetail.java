package org.cmdbuild.service.rest.cxf.serialization;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.service.rest.dto.SimpleDomainDetail;

import com.google.common.base.Function;

public class ToSimpleDomainDetail implements Function<CMDomain, SimpleDomainDetail> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToSimpleDomainDetail> {

		private Builder() {
			// use static method
		}

		@Override
		public ToSimpleDomainDetail build() {
			return new ToSimpleDomainDetail(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private ToSimpleDomainDetail(final Builder builder) {
		// nothing to do
	}

	@Override
	public SimpleDomainDetail apply(final CMDomain input) {
		return SimpleDomainDetail.newInstance() //
				.withName(input.getName()) //
				.withDescription(input.getDescription()) //
				.build();
	}

}
