package org.cmdbuild.servlets.json.schema;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.LookupType;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.data.LookupLogic;
import org.cmdbuild.logic.data.LookupLogic.LookupTypeDto;
import org.cmdbuild.operation.management.LookupOperation;
import org.cmdbuild.operation.schema.LookupTypeOperation;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModLookup extends JSONBase {

	LookupTypeOperation lookupTypeOperation = new LookupTypeOperation();

	@JSONExported
	public JSONArray tree() throws JSONException {
		final LookupLogic logic = TemporaryObjectsBeforeSpringDI.getLookupTypeLogic();
		final Iterable<LookupTypeDto> elements = logic.getAllTypes();

		final JSONArray jsonLookupTypes = new JSONArray();
		for (final LookupTypeDto element : elements) {
			jsonLookupTypes.put(Serializer.serializeLookupTable(element));
		}

		return jsonLookupTypes;
	}

	@JSONExported
	@Admin
	public JSONObject saveLookupType( //
			final JSONObject serializer, //
			final @Parameter(PARAMETER_DESCRIPTION) String type, //
			final @Parameter(PARAMETER_ORIG_TYPE) String originalType, //
			final @Parameter(value = PARAMETER_PARENT, required = false) String parentType //
	) throws JSONException {
		final LookupTypeDto newType = new LookupTypeDto(type, parentType);
		final LookupTypeDto oldType = new LookupTypeDto(originalType, parentType);
		final LookupLogic logic = TemporaryObjectsBeforeSpringDI.getLookupTypeLogic();
		logic.saveLookupType(newType, oldType);

		final JSONObject jsonLookupType = Serializer.serializeLookupTable(newType);
		serializer.put("lookup", jsonLookupType);
		if (isNotEmpty(originalType)) {
			jsonLookupType.put("oldId", originalType);
		} else {
			serializer.put("isNew", true);
		}
		return serializer;
	}

	@JSONExported
	public JSONObject getLookupList(final JSONObject serializer, final LookupOperation lo,
			@Parameter("type") final String lookupType, @Parameter(value = "start", required = false) final int start,
			@Parameter(value = "limit", required = false) final int limit, @Parameter("active") final boolean active,
			@Parameter(value = "short", required = false) final boolean shortForm) throws JSONException {
		final List<Lookup> list = lo.getLookupList(lookupType);

		// order by number
		Collections.sort(list, new Comparator<Lookup>() {
			@Override
			public int compare(final Lookup l1, final Lookup l2) {
				if (l1.getNumber() > l2.getNumber()) {
					return 1;
				} else if (l1.getNumber() < l2.getNumber()) {
					return -1;
				}
				return 0;
			}
		});

		if (list == null) {
			throw NotFoundExceptionType.LOOKUP_TYPE_NOTFOUND.createException(lookupType);
		}
		int i = 0;
		final int end = limit > 0 ? limit + start : Integer.MAX_VALUE;
		for (final Lookup lookup : list) {
			if (!active || lookup.getStatus().isActive()) {
				if (i >= start && i < end) {
					serializer.append("rows", Serializer.serializeLookup(lookup, shortForm));
				}
				++i;
			}
		}
		serializer.put("total", i);
		return serializer;
	}

	@JSONExported
	public JSONObject getParentList(final JSONObject serializer, final LookupOperation lo,
			@Parameter(value = "type", required = false) final String type) throws JSONException, AuthException {
		if (type != null && !type.equals("")) {
			final LookupType lookupType = lookupTypeOperation.getLookupType(type);
			String parentType = "";
			if (lookupType != null) {
				parentType = lookupType.getParentTypeName();
			}
			if (parentType != null && !(parentType.trim().equals(""))) {
				final Iterable<Lookup> list = lo.getLookupList(parentType);
				if (list == null) {
					throw NotFoundExceptionType.LOOKUP_NOTFOUND.createException(parentType);
				}
				// Serialize result
				for (final Lookup lookup : list) {
					serializer.append("rows", Serializer.serializeLookupParent(lookup));
				}
			}
		}
		return serializer;
	}

	@JSONExported
	@Admin
	public void disableLookup(@Parameter("Id") final int id, final LookupOperation lo) throws JSONException {
		if (id > 0) {
			lo.disableLookup(id);
		}
	}

	@JSONExported
	@Admin
	public void enableLookup(@Parameter("Id") final int id, final LookupOperation lo) throws JSONException {
		if (id > 0) {
			lo.enableLookup(id);
		}
	}

	@JSONExported
	@Admin
	public JSONObject saveLookup(final JSONObject serializer, final LookupOperation lo,
			@Parameter("Type") final String type, @Parameter("Code") final String code,
			@Parameter("Description") final String description, @Parameter("Id") final int id,
			@Parameter("ParentId") final int parentId, @Parameter("Notes") final String notes,
			@Parameter("Default") final boolean isDefault, @Parameter("Active") final boolean isActive,
			@Parameter("Number") final int number) throws JSONException {
		Lookup lookup;
		if (id == 0) {
			lookup = lo.createLookup(type, code, description, notes, parentId, number, isDefault, isActive);
		} else {
			lookup = lo.updateLookup(id, type, code, description, notes, parentId, number, isDefault, isActive);
		}
		serializer.put("lookup", Serializer.serializeLookup(lookup));
		return serializer;
	}

	@JSONExported
	@Admin
	public void reorderLookup(@Parameter("type") final String lookupType, final LookupOperation lo,
			@Parameter("lookuplist") final JSONArray decoder) throws JSONException, AuthException {
		final Map<Integer, Integer> lookupPositions = new HashMap<Integer, Integer>();
		for (int i = 0; i < decoder.length(); i++) {
			final JSONObject jattr = decoder.getJSONObject(i);
			final int lookupId = jattr.getInt("id");
			final int lookupIndex = jattr.getInt("index");
			lookupPositions.put(lookupId, lookupIndex);
		}
		lo.reorderLookup(lookupType, lookupPositions);
	}
}
