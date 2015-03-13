package org.cmdbuild.servlets.json.serializers;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.data.store.Storables.storableOf;
import static org.cmdbuild.logic.translation.DefaultTranslationLogic.DESCRIPTION_FOR_CLIENT;
import static org.cmdbuild.servlets.json.CommunicationConstants.DEFAULT_DESCRIPTION_CAPITAL;
import static org.cmdbuild.servlets.json.CommunicationConstants.DEFAULT_PARENT_DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION_CAPITAL;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID_CAPITAL;
import static org.cmdbuild.servlets.json.CommunicationConstants.PARENT_DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.PARENT_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.TRANSLATION_UUID;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.data.store.lookup._Lookup;
import org.cmdbuild.logic.translation.LookupTranslation;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.json.JSONException;
import org.json.JSONObject;

public class LookupSerializer {

	private final TranslationFacade translationFacade;
	private final LookupStore lookupStore;
	private static final String MULTILEVEL_FORMAT = "%s - %s";

	public LookupSerializer(final TranslationFacade translationFacade, final LookupStore lookupStore) {
		this.translationFacade = translationFacade;
		this.lookupStore = lookupStore;
	}

	public JSONObject serializeLookup(final _Lookup lookup) throws JSONException {
		return serializeLookup(lookup, false);
	}

	public JSONObject serializeLookup(final _Lookup lookup, final boolean shortForm) throws JSONException {
		JSONObject serializer = null;
		if (lookup != null) {
			serializer = new JSONObject();
			serializer.put(ID_CAPITAL, lookup.getId());

			final LookupTranslation translationObject = LookupTranslation.newInstance() //
					.withField(DESCRIPTION_FOR_CLIENT) //
					.withName(lookup.getTranslationUuid()) //
					.build();

			final String translatedDescription = translationFacade.read(translationObject);

			serializer.put(DESCRIPTION_CAPITAL, defaultIfBlank(translatedDescription, lookup.description()));
			serializer.put(DEFAULT_DESCRIPTION_CAPITAL, lookup.description());

			if (!shortForm) {
				serializer.put("Type", lookup.type().name);
				serializer.put("Code", defaultIfBlank(lookup.code(), EMPTY));
				serializer.put("Number", lookup.number());
				serializer.put("Notes", lookup.notes());
				serializer.put("Default", lookup.isDefault());
				serializer.put("Active", lookup.active());

				serializer.put("TranslationUuid", lookup.uuid());
			}

			final _Lookup parent = lookup.parent();
			if (parent != null) {
				serializer.put("ParentId", parent.getId());
				if (!shortForm) {

					final LookupTranslation parentTranslationObject = LookupTranslation.newInstance() //
							.withField(DESCRIPTION_FOR_CLIENT) //
							.withName(parent.getId() != null ? parent.getId().toString() : EMPTY) //
							.build();

					final String parentTranslatedDescription = translationFacade.read(parentTranslationObject);
					serializer.put(PARENT_DESCRIPTION, defaultIfBlank(parentTranslatedDescription, parent.description()));
					serializer.put(DEFAULT_PARENT_DESCRIPTION,
							defaultIfBlank(parentTranslatedDescription, parent.description()));
					serializer.put("ParentType", parent.type());
				}
			}
		}
		return serializer;
	}

	public JSONObject serializeLookupParent(final _Lookup lookup) throws JSONException {
		JSONObject serializer = null;
		if (lookup != null) {

			final LookupTranslation parentTranslationObject = LookupTranslation.newInstance() //
					.withField(DESCRIPTION_FOR_CLIENT) //
					.withName(lookup.getId() != null ? lookup.getId().toString() : EMPTY) //
					.build();

			final String parentTranslatedDescription = translationFacade.read(parentTranslationObject);

			serializer = new JSONObject();
			serializer.put(PARENT_ID, lookup.getId());
			serializer.put(PARENT_DESCRIPTION, defaultIfBlank(parentTranslatedDescription, lookup.description()));
		}
		return serializer;
	}

	public static JSONObject serializeLookupTable(final LookupType lookupType) throws JSONException {
		final JSONObject serializer = new JSONObject();
		serializer.put("id", lookupType.name);
		serializer.put("text", lookupType.name);
		serializer.put("type", "lookuptype");
		serializer.put("selectable", true);

		if (lookupType.parent != null) {
			serializer.put("parent", lookupType.parent);
		}
		return serializer;
	}

	public Map<String, Object> serializeLookupValue( //
			final LookupValue value //
	) {
		final _Lookup lookup = lookup(value.getId());
		final Map<String, Object> out = new HashMap<String, Object>();
		out.put(ID, value.getId());
		out.put(DESCRIPTION, description(value));
		out.put(TRANSLATION_UUID, (lookup == null) ? null : lookup.uuid());
		return out;
	}

	private String description(final LookupValue value) {
		final String uuid = lookup(value.getId()) == null ? null : lookup(value.getId()).uuid();
		final LookupTranslation lookupTranslation = LookupTranslation.newInstance() //
				.withField(DESCRIPTION_FOR_CLIENT).withName(uuid)//
				.build();

		final String lastLevelBaseDescription = value.getDescription();
		final String lastLevelTranslatedDescription = translationFacade.read(lookupTranslation);

		String baseDescription = lastLevelBaseDescription;
		String translatedDescription = defaultIfBlank(lastLevelTranslatedDescription, lastLevelBaseDescription);
		String jointBaseDescription = lastLevelBaseDescription;
		String jointTranslatedDescription = translatedDescription;

		if (value instanceof LookupValue) {
			_Lookup lookup = lookup(value.getId());
			if (lookup != null) {
				lookup = lookup(lookup.parentId());
				while (lookup != null) {
					final String parentBaseDescription = lookup.description();
					final LookupTranslation parentTranslation = LookupTranslation.newInstance()//
							.withField(DESCRIPTION_FOR_CLIENT)//
							.withName(lookup.getTranslationUuid() != null ? lookup.getTranslationUuid() : EMPTY)//
							.build();
					final String parentTranslatedDescription = translationFacade.read(parentTranslation);
					jointBaseDescription = String.format(MULTILEVEL_FORMAT, parentBaseDescription, baseDescription);
					jointTranslatedDescription = String.format(MULTILEVEL_FORMAT,
							defaultIfBlank(parentTranslatedDescription, parentBaseDescription),
							defaultIfBlank(translatedDescription, baseDescription));
					lookup = lookup(lookup.parentId());
					baseDescription = jointBaseDescription;
					translatedDescription = jointTranslatedDescription;
				}
			}
		}
		return jointTranslatedDescription;
	}

	private _Lookup lookup(final Long id) {
		if (id != null) {
			return lookupStore.read(storableOf(id));
		} else {
			return null;
		}
	}

}
