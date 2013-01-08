package org.cmdbuild.logic.data;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Simple DTO that represents the options for a query in CMDBuild
 */
public class QueryOptions {

	public static class QueryOptionsBuilder {

		private int limit;
		private int offset;
		private JSONObject filter;
		private JSONArray sorters;

		private QueryOptionsBuilder() {
			limit = Integer.MAX_VALUE;
			offset = 0;
			filter = new JSONObject();
			sorters = new JSONArray();
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

		public QueryOptions build() {
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

	private QueryOptions(final QueryOptionsBuilder builder) {
		this.limit = builder.limit;
		this.offset = builder.offset;
		this.filter = builder.filter;
		this.sorters = builder.sorters;
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

}
