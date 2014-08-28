package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.constants.Serialization.DATA;
import static org.cmdbuild.service.rest.constants.Serialization.SIMPLE_RESPONSE;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement(name = SIMPLE_RESPONSE)
public class SimpleResponse<T> {

	public static class Builder<T> implements org.apache.commons.lang3.builder.Builder<SimpleResponse<T>> {

		private T element;

		private Builder() {
			// use factory method
		}

		@Override
		public SimpleResponse<T> build() {
			return new SimpleResponse<T>(this);
		}

		public Builder<T> withElement(final T element) {
			this.element = element;
			return this;
		}

	}

	public static <T> Builder<T> newInstance(final Class<T> type) {
		return newInstance();
	}

	public static <T> Builder<T> newInstance() {
		return new Builder<T>();
	}

	private T element;

	SimpleResponse() {
		// package visibility
	}

	private SimpleResponse(final Builder<T> builder) {
		this.element = builder.element;
	}

	@XmlElement(name = DATA)
	@JsonProperty(DATA)
	public T getElement() {
		return element;
	}

	void setElement(final T element) {
		this.element = element;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof SimpleResponse)) {
			return false;
		}

		@SuppressWarnings("unchecked")
		final SimpleResponse<T> other = SimpleResponse.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.element, other.element) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(this.element) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
