package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.dto.Constants.DATA;

import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonProperty;

public abstract class SimpleResponse<T> {

	public static abstract class Builder<T, R extends SimpleResponse<T>> implements
			org.apache.commons.lang3.builder.Builder<SimpleResponse<T>> {

		protected T element;

		protected Builder() {
			// usable by sublasses only
		}

		@Override
		public R build() {
			return doBuild();
		}

		protected abstract R doBuild();

		public Builder<T, R> withElement(final T element) {
			this.element = element;
			return this;
		}

	}

	private T element;

	SimpleResponse() {
		// package visibility
	}

	protected SimpleResponse(final Builder<T, ?> builder) {
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
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
