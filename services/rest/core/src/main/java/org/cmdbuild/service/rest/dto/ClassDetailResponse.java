package org.cmdbuild.service.rest.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.collect.Lists;

public class ClassDetailResponse {

	public static class Builder implements org.cmdbuild.common.Builder<ClassDetailResponse> {

		private Iterable<ClassDetail> details;
		private int total;

		private Builder() {
			// use static method
		}

		@Override
		public ClassDetailResponse build() {
			return new ClassDetailResponse(this);
		}

		public Builder withDetails(final Iterable<ClassDetail> details) {
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

	@XmlAttribute(name = "data")
	@JsonProperty("data")
	private final List<ClassDetail> details;
	private final int total;

	public ClassDetailResponse(final Builder builder) {
		this.details = Lists.newArrayList(builder.details);
		this.total = builder.total;
	}

	public Iterable<ClassDetail> getDetails() {
		return details;
	}

	public int getTotal() {
		return total;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
