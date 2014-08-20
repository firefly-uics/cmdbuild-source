package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.constants.Serialization.DATA;
import static org.cmdbuild.service.rest.constants.Serialization.LIST_RESPONSE;
import static org.cmdbuild.service.rest.constants.Serialization.RESPONSE_METADATA;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@XmlRootElement(name = LIST_RESPONSE)
public class ListResponse<T> {

	public static class Builder<T> implements org.apache.commons.lang3.builder.Builder<ListResponse<T>> {

		private Iterable<T> elements;
		private DetailResponseMetadata metadata;

		private Builder() {
			// use factory method
		}

		@Override
		public ListResponse<T> build() {
			return new ListResponse<T>(this);
		}

		public Builder<T> withElements(final Iterable<T> elements) {
			this.elements = elements;
			return this;
		}

		public Builder<T> withMetadata(final DetailResponseMetadata metadata) {
			this.metadata = metadata;
			return this;
		}

	}

	public static <T> Builder<T> newInstance() {
		return new Builder<T>();
	}

	private Collection<T> elements;
	private DetailResponseMetadata metadata;

	ListResponse() {
		// package visibility
	}

	private ListResponse(final Builder<T> builder) {
		this.elements = Lists.newArrayList(builder.elements);
		this.metadata = builder.metadata;
	}

	@XmlElement(name = DATA)
	@JsonProperty(DATA)
	public Collection<T> getElements() {
		return elements;
	}

	void setElements(final Iterable<T> elements) {
		this.elements = Sets.newHashSet(elements);
	}

	@XmlElement(name = RESPONSE_METADATA, type = DetailResponseMetadata.class)
	@JsonProperty(RESPONSE_METADATA)
	public DetailResponseMetadata getMetadata() {
		return metadata;
	}

	void setMetadata(final DetailResponseMetadata metadata) {
		this.metadata = metadata;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
