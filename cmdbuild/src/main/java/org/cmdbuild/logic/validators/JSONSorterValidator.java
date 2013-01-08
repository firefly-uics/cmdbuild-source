package org.cmdbuild.logic.validators;

import static org.cmdbuild.logic.mappers.json.Constants.*;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class JSONSorterValidator implements Validator {

	private static final String MALFORMED_MSG = "Malformed sorters"; 
	private final JSONArray sorters;

	public JSONSorterValidator(final JSONArray sorters) {
		this.sorters = sorters;
	}

	public void validate() {
		try {
			for (int i = 0; i < sorters.length(); i++) {
				final JSONObject sorterObject = sorters.getJSONObject(i);
				if (!sorterObject.has(PROPERTY_KEY) || !sorterObject.has(DIRECTION_KEY)) {
					throw new IllegalArgumentException(MALFORMED_MSG);
				}
				validateDirectionValue(sorterObject);
			}
		} catch (final Exception ex) {
			throw new IllegalArgumentException(MALFORMED_MSG + ex.getMessage());
		}
	}

	private void validateDirectionValue(final JSONObject sorter) throws JSONException {
		final String direction = sorter.getString(DIRECTION_KEY);
		if (direction.equalsIgnoreCase(Direction.ASC.toString())
				|| direction.equalsIgnoreCase(Direction.DESC.toString())) {
			return;
		}
		throw new IllegalArgumentException("Direction value must be one of these values: ASC, DESC");
	}

}
