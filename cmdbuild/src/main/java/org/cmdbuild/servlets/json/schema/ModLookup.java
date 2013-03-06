package org.cmdbuild.servlets.json.schema;

import static com.google.common.collect.Iterables.size;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.util.Map;

import org.cmdbuild.exception.AuthException;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.data.lookup.LookupDto;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.data.lookup.LookupTypeDto;
import org.cmdbuild.operation.management.LookupOperation;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.LookupSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Maps;

import static org.cmdbuild.servlets.json.ComunicationConstants.*;

public class ModLookup extends JSONBase {

	@JSONExported
	public JSONArray tree() throws JSONException {
		final Iterable<LookupTypeDto> elements = lookupLogic().getAllTypes();

		final JSONArray jsonLookupTypes = new JSONArray();
		for (final LookupTypeDto element : elements) {
			jsonLookupTypes.put(LookupSerializer.serializeLookupTable(element));
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
		lookupLogic().saveLookupType(newType, oldType);

		final JSONObject jsonLookupType = LookupSerializer.serializeLookupTable(newType);
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
		final Iterable<LookupDto> elements = lookupLogic().getAllLookup(lookupType, active, start, limit);

		for (final LookupDto element : elements) {
			serializer.append("rows", LookupSerializer.serializeLookup(element, shortForm));
		}
		serializer.put("total", size(elements));
		return serializer;
	}

	@JSONExported
	public JSONObject getParentList( //
			final JSONObject serializer, //
			final LookupOperation lo, //
			final @Parameter(value = PARAMETER_TYPE, required = false) String type //
	) throws JSONException, AuthException {
		final LookupTypeDto lookupType = LookupTypeDto.newInstance().withName(type).build();
		final Iterable<LookupDto> elements = lookupLogic().getAllLookupOfParent(lookupType);

		for (final LookupDto lookup : elements) {
			serializer.append("rows", LookupSerializer.serializeLookupParent(lookup));
		}
		return serializer;
	}

	@JSONExported
	@Admin
	public void disableLookup( //
			@Parameter(PARAMETER_ID) final int id //
	) throws JSONException {
		lookupLogic().disableLookup(Long.valueOf(id));
	}

	@JSONExported
	@Admin
	public void enableLookup( //
			@Parameter(PARAMETER_ID) final int id //
	) throws JSONException {
		lookupLogic().enableLookup(Long.valueOf(id));
	}

	@JSONExported
	@Admin
	public JSONObject saveLookup( //
			final JSONObject serializer, //
			final @Parameter(PARAMETER_TYPE_CAPITAL) String type, //
			final @Parameter(PARAMETER_CODE_CAPITAL) String code, //
			final @Parameter(PARAMETER_DESCRIPTION_CAPITAL) String description, //
			final @Parameter(PARAMETER_ID_CAPITAL) int id, //
			final @Parameter(PARAMETER_PARENT_ID) int parentId, //
			final @Parameter(PARAMETER_NOTES) String notes, //
			final @Parameter(PARAMETER_DEFAULT) boolean isDefault, //
			final @Parameter(PARAMETER_ACTIVE_CAPITAL) boolean isActive, //
			final @Parameter(PARAMETER_NUMBER) int number //
	) throws JSONException {
		final LookupDto lookup = LookupDto.newInstance() //
				.withId(Long.valueOf(id)) //
				.withCode(code) //
				.withDescription(description) //
				.withType(LookupTypeDto.newInstance() //
						.withName(type)) //
				.withParentId(Long.valueOf(parentId)) //
				.withNotes(notes) //
				.withDefaultStatus(isDefault) //
				.withActiveStatus(isActive) //
				.build();
		lookupLogic().createOrUpdateLookup(lookup);

		serializer.put("lookup", LookupSerializer.serializeLookup(lookup));
		return serializer;
	}

	@JSONExported
	@Admin
	public void reorderLookup( //
			final @Parameter(PARAMETER_TYPE) String type, //
			final @Parameter(PARAMETER_LOOKUP_LIST) JSONArray jsonPositions //
	) throws JSONException, AuthException {
		final LookupTypeDto lookupType = LookupTypeDto.newInstance() //
				.withName(type) //
				.build();
		final Map<Long, Integer> positions = Maps.newHashMap();
		for (int i = 0; i < jsonPositions.length(); i++) {
			final JSONObject jsonElement = jsonPositions.getJSONObject(i);
			positions.put( //
					Long.valueOf(jsonElement.getInt("id")), //
					jsonElement.getInt("index"));
		}
		lookupLogic().reorderLookup(lookupType, positions);
	}

	private LookupLogic lookupLogic() {
		return TemporaryObjectsBeforeSpringDI.getLookupLogic();
	}

}
