package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.dto.Constants.CARD_DETAIL;
import static org.cmdbuild.service.rest.dto.Constants.DESCRIPTION;
import static org.cmdbuild.service.rest.dto.Constants.ID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = CARD_DETAIL)
public class CardDetail {

	public static class Builder implements org.cmdbuild.common.Builder<CardDetail> {

		private Long id;
		private String description;

		private Builder() {
			// use static method
		}

		@Override
		public CardDetail build() {
			return new CardDetail(this);
		}

		public Builder withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private Long id;
	private String description;

	CardDetail() {
		// package visibility
	}

	private CardDetail(final Builder builder) {
		this.id = builder.id;
		this.description = builder.description;
	}

	@XmlAttribute(name = ID)
	public Long getId() {
		return id;
	}

	void setId(final Long id) {
		this.id = id;
	}

	@XmlAttribute(name = DESCRIPTION)
	public String getDescription() {
		return description;
	}

	void setDescription(final String description) {
		this.description = description;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof CardDetail)) {
			return false;
		}

		final CardDetail other = CardDetail.class.cast(obj);
		return id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
