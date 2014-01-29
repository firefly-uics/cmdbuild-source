package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.dto.Constants.DATA;
import static org.cmdbuild.service.rest.dto.Constants.LOOKUP_TYPE_DETAIL_RESPONSE;
import static org.cmdbuild.service.rest.dto.Constants.TOTAL;

import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.collect.Sets;

@XmlRootElement(name = LOOKUP_TYPE_DETAIL_RESPONSE)
public class LookupDetailResponse {

	public static class Builder implements org.cmdbuild.common.Builder<LookupDetailResponse> {

		private Iterable<LookupDetail> details;
		private int total;

		private Builder() {
			// use static method
		}

		@Override
		public LookupDetailResponse build() {
			return new LookupDetailResponse(this);
		}

		public Builder withDetails(final Iterable<LookupDetail> details) {
			this.details = details;
			return this;
		}

		public Builder withTotal(final int total) {
			this.total = total;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private Set<LookupDetail> details;
	private int total;

	LookupDetailResponse() {
		// package visibility
	}

	private LookupDetailResponse(final Builder builder) {
		this.details = Sets.newHashSet(builder.details);
		this.total = builder.total;
	}

	@XmlElement(name = DATA, type = LookupDetail.class)
	@JsonProperty(DATA)
	public Set<LookupDetail> getDetails() {
		return details;
	}

	void setDetails(final Iterable<LookupDetail> details) {
		this.details = Sets.newHashSet(details);
	}

	@XmlAttribute(name = TOTAL)
	public int getTotal() {
		return total;
	}

	void setTotal(final int total) {
		this.total = total;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
