package org.cmdbuild.services.store;

import org.cmdbuild.common.Builder;
import org.cmdbuild.services.store.FilterStore.Filter;

public class FilterDTO implements Filter {

	public static class FilterDTOBuilder implements Builder<FilterDTO> {
		private String id;
		private String name;
		private String description;
		private String value;
		private String className;

		private FilterDTOBuilder() {
		}

		public FilterDTOBuilder withId(final String id) {
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

		public FilterDTOBuilder withValue(final String value) {
			this.value = value;
			return this;
		}

		public FilterDTOBuilder forClass(final String className) {
			this.className = className;
			return this;
		}

		@Override
		public FilterDTO build() {
			return new FilterDTO(this);
		}
	}

	private final String id;
	private final String name;
	private final String description;
	private final String value;
	private final String className;

	private FilterDTO(final FilterDTOBuilder builder) {
		this.name = builder.name;
		this.description = builder.description;
		this.value = builder.value;
		this.className = builder.className;
		this.id = builder.id;
	}

	public static FilterDTOBuilder newFilter() {
		return new FilterDTOBuilder();
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
	public String getValue() {
		return value;
	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public String getId() {
		return id;
	}

}
