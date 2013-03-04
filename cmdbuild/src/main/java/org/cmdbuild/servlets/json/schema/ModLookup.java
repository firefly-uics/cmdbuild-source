package org.cmdbuild.servlets.json.schema;

import static com.google.common.collect.Iterables.size;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.LookupType;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.data.LookupLogic;
import org.cmdbuild.logic.data.LookupLogic.LookupDto;
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
		final LookupTypeDto newType = LookupTypeDto.newInstance().withName(type).withParent(parentType).build();
		final LookupTypeDto oldType = LookupTypeDto.newInstance().withName(originalType).withParent(parentType).build();
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
	public JSONObject getLookupList( //
			final JSONObject serializer, //
			final @Parameter(PARAMETER_TYPE) String type, //
			final @Parameter(value = PARAMETER_START, required = false) int start, //
			final @Parameter(value = PARAMETER_LIMIT, required = false) int limit, //
			final @Parameter(PARAMETER_ACTIVE) boolean active, //
			final @Parameter(value = PARAMETER_SHORT, required = false) boolean shortForm) //
			throws JSONException {
		final LookupTypeDto lookupType = LookupTypeDto.newInstance().withName(type).build();
		final LookupLogic logic = TemporaryObjectsBeforeSpringDI.getLookupTypeLogic();
		final Iterable<LookupDto> elements = logic.getAllLookup(lookupType, active, start, limit);

		for (final LookupDto element : elements) {
			serializer.append("rows", Serializer.serializeLookup(element, shortForm));
		}
		serializer.put("total", size(elements));
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
