package org.cmdbuild.logic.data;

import java.util.Map;

import org.cmdbuild.common.Builder;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Maps;

/**
 * Simple DTO that represents the options for a query in CMDBuild
 */
public class QueryOptions {

	public static class QueryOptionsBuilder implements Builder<QueryOptions> {

		private int limit;
		private int offset;
		private JSONObject filter;
		private JSONArray sorters;
		private JSONArray attributeSubset;
		private Map<String, Object> otherAttributes;

		private QueryOptionsBuilder() {
			limit = Integer.MAX_VALUE;
			offset = 0;
			filter = new JSONObject();
			sorters = new JSONArray();
			attributeSubset = new JSONArray();
			otherAttributes = Maps.newHashMap();
		}

		public QueryOptionsBuilder limit(final int limit) {
			this.limit = limit;
			return this;
		}

		public QueryOptionsBuilder offset(final int offset) {
			this.offset = offset;
			return this;
		}

		public QueryOptionsBuilder orderBy(final JSONArray sorters) {
			if (sorters == null) {
				this.sorters = new JSONArray();
			} else {
				this.sorters = sorters;
			}
			return this;
		}

		public QueryOptionsBuilder filter(final JSONObject filter) {
			if (filter == null) {
				this.filter = new JSONObject();
			} else {
				this.filter = filter;
			}
			return this;
		}

		public QueryOptionsBuilder onlyAttributes(final JSONArray attributes) {
			if (attributes == null) {
				this.attributeSubset = new JSONArray();
			} else {
				this.attributeSubset = attributes;
			}
			return this;
		}

		public QueryOptionsBuilder attributes(final Map<String, Object> otherAttributes) {
			this.otherAttributes = otherAttributes;
			return this;
		}

		@Override
		public QueryOptions build() {
			if (offset == 0 && limit == 0) {
				limit = Integer.MAX_VALUE;
			}
			return new QueryOptions(this);
		}

	}

	public static QueryOptionsBuilder newQueryOption() {
		return new QueryOptionsBuilder();
	}

	private final int limit;
	private final int offset;
	private final JSONObject filter;
	private final JSONArray sorters;
	private final JSONArray attributes;
	private final Map<String, Object> otherAttributes;

	private QueryOptions(final QueryOptionsBuilder builder) {
		this.limit = builder.limit;
		this.offset = builder.offset;
		this.filter = builder.filter;
		this.sorters = builder.sorters;
		this.attributes = builder.attributeSubset;
		this.otherAttributes = builder.otherAttributes;
	}

	public int getLimit() {
		return limit;
	}

	public int getOffset() {
		return offset;
	}

	public JSONObject getFilter() {
		return filter;
	}

	public JSONArray getSorters() {
		return sorters;
	}

	public JSONArray getAttributes() {
		return attributes;
	}

	public Map<String, Object> getOtherAttributes() {
		return otherAttributes;
	}

}
