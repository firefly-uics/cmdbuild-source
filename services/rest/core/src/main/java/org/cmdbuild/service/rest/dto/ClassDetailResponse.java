package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.dto.Constants.NAMESPACE;

import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.collect.Sets;

@XmlRootElement(name = "classDetailResponse", namespace = NAMESPACE)
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

	private Set<ClassDetail> details;
	private int total;

	ClassDetailResponse() {
		// package visibility
	}

	private ClassDetailResponse(final Builder builder) {
		this.details = Sets.newHashSet(builder.details);
		this.total = builder.total;
	}

	@XmlElement(name = "data", type = ClassDetail.class)
	@JsonProperty("data")
	public Set<ClassDetail> getDetails() {
		return details;
	}

	void setDetails(final Iterable<ClassDetail> details) {
		this.details = Sets.newHashSet(details);
	}

	@XmlAttribute
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
