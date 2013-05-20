package org.cmdbuild.logic.mapping.json;

import static org.cmdbuild.logic.mapping.json.Constants.Filters.CQL_KEY;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.common.Builder;
import org.cmdbuild.cql.sqlbuilder.CQLFacadeCompiler;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.mapping.FilterMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class JsonFilterMapper implements FilterMapper {

	private static final Logger logger = Log.CMDBUILD;
	private static final Marker marker = MarkerFactory.getMarker(JsonFilterMapper.class.getName());

	public static class JsonFilterMapperBuilder implements Builder<JsonFilterMapper> {

		private static final Marker marker = MarkerFactory.getMarker(JsonFilterMapperBuilder.class.getName());

		private CMDataView dataView;
		private CMEntryType entryType;
		private Alias entryTypeAlias;
		private JSONObject filterObject;
		private Map<String, Object> otherAttributes;

		private FilterMapper inner;

		@Override
		public JsonFilterMapper build() {
			Validate.notNull(entryType);
			Validate.notNull(filterObject);
			if (filterObject.has(CQL_KEY)) {
				try {
					logger.info(marker, "filter is CQL filter");
					final String cql = filterObject.getString(CQL_KEY);
					final Map<String, Object> context = otherAttributes;
					inner = CQLFacadeCompiler.compile(cql, context);
				} catch (final JSONException e) {
					throw new IllegalArgumentException("error getting CQL string");
				}
			} else {
				logger.info(marker, "filter is advanced filter");
				inner = new JsonAdvancedFilterMapper(entryType, filterObject, dataView, entryTypeAlias);
			}
			return new JsonFilterMapper(this);
		}

		public JsonFilterMapperBuilder withDataView(final CMDataView dataView) {
			this.dataView = dataView;
			return this;
		}

		public JsonFilterMapperBuilder withEntryType(final CMEntryType entryType) {
			this.entryType = entryType;
			return this;
		}

		public JsonFilterMapperBuilder withEntryTypeAlias(final Alias entryTypeAlias) {
			this.entryTypeAlias = entryTypeAlias;
			return this;
		}

		public JsonFilterMapperBuilder withFilterObject(final JSONObject filterObject) {
			this.filterObject = filterObject;
			return this;
		}

		public JsonFilterMapperBuilder withOtherAttributes(final Map<String, Object> otherAttributes) {
			this.otherAttributes = otherAttributes;
			return this;
		}

	}

	public static JsonFilterMapperBuilder newInstance() {
		return new JsonFilterMapperBuilder();
	}

	private final FilterMapper inner;

	private JsonFilterMapper(final JsonFilterMapperBuilder builder) {
		this.inner = builder.inner;
	}

	@Override
	public CMEntryType entryType() {
		logger.info(marker, "getting entry type");
		return inner.entryType();
	}

	@Override
	public WhereClause whereClause() {
		logger.info(marker, "getting where clause");
		return inner.whereClause();
	}

	@Override
	public Iterable<JoinElement> joinElements() {
		logger.info(marker, "getting join elements type");
		return inner.joinElements();
	}

}
