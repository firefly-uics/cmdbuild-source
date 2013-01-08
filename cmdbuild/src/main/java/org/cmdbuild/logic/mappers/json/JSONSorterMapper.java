package org.cmdbuild.logic.mappers.json;

import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.logic.mappers.json.Constants.*;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.cmdbuild.logic.mappers.SorterMapper;
import org.cmdbuild.logic.validators.JSONSorterValidator;
import org.cmdbuild.logic.validators.Validator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class JSONSorterMapper implements SorterMapper {

//	private static class Validator {
//
//		private final JSONArray sorters;
//
//		public Validator(final JSONArray sorters) {
//			this.sorters = sorters;
//		}
//
//		private boolean isValid() {
//			try {
//				for (int i = 0; i < sorters.length(); i++) {
//					final JSONObject sorterObject = sorters.getJSONObject(i);
//					if (!sorterObject.has(PROPERTY_KEY) || !sorterObject.has(DIRECTION_KEY)) {
//						return false;
//					}
//					validateDirectionValue(sorterObject);
//				}
//				return true;
//			} catch (final Exception ex) {
//				throw new IllegalArgumentException("Malformed sorter: " + ex.getMessage());
//			}
//		}
//
//		private void validateDirectionValue(final JSONObject sorter) throws JSONException {
//			final String direction = sorter.getString(DIRECTION_KEY);
//			if (direction.equalsIgnoreCase(Direction.ASC.toString())
//					|| direction.equalsIgnoreCase(Direction.DESC.toString())) {
//				return;
//			}
//			throw new IllegalArgumentException("Direction value is neither ASC nor DESC");
//		}
//	}

	private final CMEntryType entryType;
	private final JSONArray sorters;
	private final Validator validator;

	public JSONSorterMapper(final CMEntryType entryType, final JSONArray sorters) {
		this.entryType = entryType;
		this.sorters = sorters;
		this.validator = new JSONSorterValidator(sorters);
	}

	@Override
	public List<OrderByClause> deserialize() {
		final List<OrderByClause> orderByClauses = Lists.newArrayList();
		if (sorters == null || sorters.length() == 0) {
			return orderByClauses;
		}
		validator.validate();
		for (int i = 0; i < sorters.length(); i++) {
			try {
				final JSONObject sorterObject = sorters.getJSONObject(i);
				final String attribute = sorterObject.getString(PROPERTY_KEY);
				final String direction = sorterObject.getString(DIRECTION_KEY);
				orderByClauses.add(new OrderByClause(attribute(entryType, attribute), Direction.valueOf(direction)));
			} catch (final JSONException ex) {
				throw new IllegalArgumentException("Malformed sorter");
			}
		}
		return orderByClauses;
	}

}
