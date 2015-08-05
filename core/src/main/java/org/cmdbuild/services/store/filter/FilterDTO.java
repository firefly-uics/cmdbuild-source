package org.cmdbuild.services.store.filter;

import static com.google.common.reflect.Reflection.newProxy;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.common.utils.Reflection.unsupported;

import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.services.localization.LocalizableStorableVisitor;
import org.cmdbuild.services.store.filter.FilterStore.Filter;

public class FilterDTO extends ForwardingFilter {

	public static class FilterDTOBuilder implements Builder<FilterDTO> {

		private Long id;
		private String name;
		private String description;
		private String value;
		private String className;
		private boolean template;

		/**
		 * Use factory method.
		 */
		private FilterDTOBuilder() {
		}

		public FilterDTOBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public FilterDTOBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public FilterDTOBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public FilterDTOBuilder forClass(final String className) {
			this.className = className;
			return this;
		}

		public FilterDTOBuilder withValue(final String value) {
			this.value = value;
			return this;
		}

		public FilterDTOBuilder asTemplate(final boolean template) {
			this.template = template;
			return this;
		}

		@Override
		public FilterDTO build() {
			return new FilterDTO(this);
		}

	}

	public static FilterDTOBuilder newFilter() {
		return new FilterDTOBuilder();
	}

	private static final Filter unsupported = newProxy(Filter.class, unsupported("should be never called"));

	private final Long id;
	private final String name;
	private final String description;
	private final String value;
	private final String className;
	private final boolean template;

	private FilterDTO(final FilterDTOBuilder builder) {
		this.name = builder.name;
		this.description = builder.description;
		this.value = builder.value;
		this.className = builder.className;
		this.id = builder.id;
		this.template = builder.template;
	}

	@Override
	protected Filter delegate() {
		return unsupported;
	}

	@Override
	public void accept(final LocalizableStorableVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String getPrivilegeId() {
		return String.format("Filter:%d", getId());
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isTemplate() {
		return template;
	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Filter)) {
			return false;
		}
		final Filter other = Filter.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.append(this.getName(), other.getName()) //
				.append(this.getDescription(), other.getDescription()) //
				.append(this.isTemplate(), other.isTemplate()) //
				.append(this.getClassName(), other.getClassName()) //
				.append(this.getValue(), other.getValue()) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(getId()) //
				.append(getName()) //
				.append(getDescription()) //
				.append(isTemplate()) //
				.append(getClassName()) //
				.append(getValue()) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return reflectionToString(this, SHORT_PREFIX_STYLE);
	}

}
