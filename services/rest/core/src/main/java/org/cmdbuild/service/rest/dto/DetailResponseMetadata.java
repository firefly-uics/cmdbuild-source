package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.constants.Serialization.RESPONSE_METADATA;
import static org.cmdbuild.service.rest.constants.Serialization.TOTAL;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = RESPONSE_METADATA)
public class DetailResponseMetadata {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<DetailResponseMetadata> {

		private Long total;

		private Builder() {
			// use factory method
		}

		@Override
		public DetailResponseMetadata build() {
			validate();
			return new DetailResponseMetadata(this);
		}

		private void validate() {
			// TODO Auto-generated method stub
		}

		public Builder withTotal(final Long total) {
			this.total = total;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private Long total;

	DetailResponseMetadata() {
		// package visibility
	}

	private DetailResponseMetadata(final Builder builder) {
		this.total = builder.total;
	}

	@XmlAttribute(name = TOTAL)
	public Long getTotal() {
		return total;
	}

	void setTotal(final Long total) {
		this.total = total;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof DetailResponseMetadata)) {
			return false;
		}
		final DetailResponseMetadata other = DetailResponseMetadata.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.total, other.total) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(this.total) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
