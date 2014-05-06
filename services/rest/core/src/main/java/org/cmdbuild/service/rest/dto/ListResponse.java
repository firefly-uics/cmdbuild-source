package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.dto.Constants.DATA;
import static org.cmdbuild.service.rest.dto.Constants.RESPONSE_METADATA;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public abstract class ListResponse<T> {

	public static abstract class Builder<T, R extends ListResponse<T>> implements
			org.cmdbuild.common.Builder<ListResponse<T>> {

		protected Iterable<T> elements;
		protected DetailResponseMetadata metadata;

		protected Builder() {
			// usable by sublasses only
		}

		@Override
		public R build() {
			return doBuild();
		}

		protected abstract R doBuild();

		public Builder<T, R> withElements(final Iterable<T> elements) {
			this.elements = elements;
			return this;
		}

		public Builder<T, R> withMetadata(final DetailResponseMetadata metadata) {
			this.metadata = metadata;
			return this;
		}

	}

	private Collection<T> elements;
	private DetailResponseMetadata metadata;

	ListResponse() {
		// package visibility
	}

	protected ListResponse(final Builder<T, ?> builder) {
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
