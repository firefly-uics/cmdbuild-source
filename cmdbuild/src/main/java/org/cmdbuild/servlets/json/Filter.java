package org.cmdbuild.servlets.json;

import static org.apache.commons.lang3.builder.ToStringBuilder.*;
import static org.apache.commons.lang3.builder.ToStringStyle.*;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.CONFIGURATION;
import static org.cmdbuild.servlets.json.CommunicationConstants.COUNT;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.ENTRY_TYPE;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTERS;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.LIMIT;
import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.POSITION;
import static org.cmdbuild.servlets.json.CommunicationConstants.START;
import static org.cmdbuild.servlets.json.CommunicationConstants.TEMPLATE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.filter.FilterLogic;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Filter extends JSONBaseWithSpringContext {

	private static class FilterImpl implements FilterLogic.Filter {

		private static class Builder implements org.apache.commons.lang3.builder.Builder<FilterImpl> {

			private Long id;
			private String name;
			private String description;
			private String className;
			private String configuration;
			private boolean template;

			/**
			 * Use factory method.
			 */
			private Builder() {
			}

			@Override
			public FilterImpl build() {
				return new FilterImpl(this);
			}

			public Builder withId(final Long id) {
				this.id = id;
				return this;
			}

			public Builder withName(final String name) {
				this.name = name;
				return this;
			}

			public Builder withDescription(final String description) {
				this.description = description;
				return this;
			}

			public Builder withClassName(final String className) {
				this.className = className;
				return this;
			}

			public Builder withConfiguration(final String configuration) {
				this.configuration = configuration;
				return this;
			}

			public Builder withTemplate(final boolean template) {
				this.template = template;
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final Long id;
		private final String name;
		private final String description;
		private final String className;
		private final String configuration;
		private final boolean template;

		private FilterImpl(final Builder builder) {
			this.id = builder.id;
			this.name = builder.name;
			this.description = builder.description;
			this.className = builder.className;
			this.configuration = builder.configuration;
			this.template = builder.template;
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
		public String getClassName() {
			return className;
		}

		@Override
		public String getConfiguration() {
			return configuration;
		}

		@Override
		public boolean isTemplate() {
			return template;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof FilterLogic.Filter)) {
				return false;
			}
			final FilterLogic.Filter other = FilterLogic.Filter.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.getId(), other.getId()) //
					.append(this.getName(), other.getName()) //
					.append(this.getDescription(), other.getDescription()) //
					.append(this.getClassName(), other.getClassName()) //
					.append(this.getConfiguration(), other.getConfiguration()) //
					.append(this.isTemplate(), other.isTemplate()) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(getId()) //
					.append(getName()) //
					.append(getDescription()) //
					.append(getClassName()) //
					.append(getConfiguration()) //
					.append(isTemplate()) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	/**
	 * Retrieves only users' filters (it does not fetches filters defined for
	 * groups)
	 * 
	 * @param start
	 *            is the offset (used for pagination)
	 * @param limit
	 *            is the max number of rows for each page (used for pagination)
	 * @return
	 * @throws JSONException
	 * @throws CMDBException
	 */
	@JSONExported
	public JSONObject read( //
			final @Parameter(value = CLASS_NAME) String className, //
			final @Parameter(value = START) int start, //
			final @Parameter(value = LIMIT) int limit //
	) throws JSONException {
		final PagedElements<FilterLogic.Filter> filters = filterLogic().getAllUserFilters(className, start, limit);
		return serialize(filters);
	}

	/**
	 * Retrieves only groups filters
	 * 
	 * @param start
	 *            is the offset (used for pagination)
	 * @param limit
	 *            is the max number of rows for each page (used for pagination)
	 * @return
	 * @throws JSONException
	 * @throws CMDBException
	 */
	@JSONExported
	public JSONObject readAllGroupFilters( //
			@Parameter(value = START) final int start, //
			@Parameter(value = LIMIT) final int limit //
	) throws JSONException {
		final PagedElements<FilterLogic.Filter> filters = filterLogic().fetchAllGroupsFilters(start, limit);
		return serialize(filters);
	}

	/**
	 * Retrieves, for the currently logged user, all filters (group and user
	 * filters) that are referred to the className
	 * 
	 * @param className
	 * @return
	 * @throws JSONException
	 */
	@JSONExported
	public JSONObject readForUser( //
			@Parameter(value = CLASS_NAME) final String className //
	) throws JSONException {
		final PagedElements<FilterLogic.Filter> filters = filterLogic().getFiltersForCurrentlyLoggedUser(className);
		return serialize(filters);
	}

	@JSONExported
	public JSONObject create( //
			@Parameter(value = NAME) final String name, //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = DESCRIPTION) final String description, //
			@Parameter(value = CONFIGURATION) final JSONObject configuration, //
			@Parameter(value = TEMPLATE, required = false) final boolean template //
	) throws JSONException {
		final FilterLogic.Filter filter = filterLogic().create(FilterImpl.newInstance() //
				.withName(name) //
				.withDescription(description) //
				.withClassName(className) //
				.withConfiguration(configuration.toString()) //
				.withTemplate(template) //
				.build());
		return serialize(filter, FILTER);
	}

	@JSONExported
	public void update( //
			@Parameter(value = ID) final Long id, //
			@Parameter(value = NAME) final String name, //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = DESCRIPTION) final String description, //
			@Parameter(value = CONFIGURATION) final JSONObject configuration //
	) {
		filterLogic().update(FilterImpl.newInstance() //
				.withId(id) //
				.withName(name) //
				.withDescription(description) //
				.withClassName(className) //
				.withConfiguration(configuration.toString()) //
				.build());
	}

	@JSONExported
	public void delete( //
			@Parameter(value = ID) final Long id //
	) {
		filterLogic().delete(FilterImpl.newInstance() //
				.withId(id) //
				.build());
	}

	@JSONExported
	public JSONObject position( //
			@Parameter(value = ID) final Long id //
	) throws JSONException {
		final Long position = filterLogic().position(FilterImpl.newInstance() //
				.withId(id) //
				.build());
		final JSONObject out = new JSONObject();
		out.put(POSITION, position);
		return out;
	}

	/**
	 * @deprecated do it using JSON mapping.
	 */
	@Deprecated
	private static JSONObject serialize(final PagedElements<FilterLogic.Filter> filters) throws JSONException {
		final JSONArray jsonFilters = new JSONArray();
		for (final FilterLogic.Filter f : filters) {
			jsonFilters.put(serialize(f));
		}
		final JSONObject out = new JSONObject();
		out.put(FILTERS, jsonFilters);
		out.put(COUNT, filters.totalSize());
		return out;
	}

	/**
	 * @deprecated do it using JSON mapping.
	 */
	@Deprecated
	private static JSONObject serialize(final FilterLogic.Filter filter) throws JSONException {
		return serialize(filter, null);
	}

	/**
	 * @deprecated do it using JSON mapping.
	 */
	@Deprecated
	private static JSONObject serialize(final FilterLogic.Filter filter, final String wrapperName) throws JSONException {
		final JSONObject jsonFilter = new JSONObject();
		jsonFilter.put(ID, filter.getId());
		jsonFilter.put(NAME, filter.getName());
		jsonFilter.put(DESCRIPTION, filter.getDescription());
		jsonFilter.put(ENTRY_TYPE, filter.getClassName());
		jsonFilter.put(TEMPLATE, filter.isTemplate());
		jsonFilter.put(CONFIGURATION, new JSONObject(filter.getConfiguration()));
		final JSONObject out;
		if (wrapperName != null) {
			out = new JSONObject();
			out.put(wrapperName, jsonFilter);
		} else {
			out = jsonFilter;
		}
		return out;
	}

}
