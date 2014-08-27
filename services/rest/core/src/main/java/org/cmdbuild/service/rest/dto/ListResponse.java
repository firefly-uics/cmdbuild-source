package org.cmdbuild.service.rest.dto;

import static com.google.common.collect.Iterables.addAll;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.service.rest.constants.Serialization.DATA;
import static org.cmdbuild.service.rest.constants.Serialization.LIST_RESPONSE;
import static org.cmdbuild.service.rest.constants.Serialization.RESPONSE_METADATA;

import java.util.Collection;
import java.util.Collections;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@XmlRootElement(name = LIST_RESPONSE)
public class ListResponse<T> {

	public static class Builder<T> implements org.apache.commons.lang3.builder.Builder<ListResponse<T>> {

		private final Iterable<T> NO_ELEMENTS = Collections.emptyList();

		private final Collection<T> elements = Lists.newArrayList();
		private DetailResponseMetadata metadata;

		private Builder() {
			// use factory method
		}

		@Override
		public ListResponse<T> build() {
			return new ListResponse<T>(this);
		}

		@SuppressWarnings("unchecked")
		public Builder<T> withElement(final T element) {
			addAll(this.elements, (element == null) ? NO_ELEMENTS : asList(element));
			return this;
		}

		public Builder<T> withElements(final Iterable<T> elements) {
			addAll(this.elements, defaultIfNull(elements, NO_ELEMENTS));
			return this;
		}

		public Builder<T> withMetadata(final DetailResponseMetadata metadata) {
			this.metadata = metadata;
			return this;
		}

	}

	public static <T> Builder<T> newInstance(final Class<T> type) {
		return newInstance();
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
		this.elements = builder.elements;
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
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ListResponse)) {
			return false;
		}

		@SuppressWarnings("unchecked")
		final ListResponse<T> other = ListResponse.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.elements, other.elements) //
				.append(this.metadata, other.metadata) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(this.elements) //
				.append(this.metadata) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
