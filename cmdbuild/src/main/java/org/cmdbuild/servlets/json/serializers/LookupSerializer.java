package org.cmdbuild.servlets.json.serializers;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.cmdbuild.logic.translation.DefaultTranslationLogic.DESCRIPTION_FOR_CLIENT;
import static org.cmdbuild.servlets.json.CommunicationConstants.DEFAULT_DESCRIPTION_CAPITAL;
import static org.cmdbuild.servlets.json.CommunicationConstants.DEFAULT_PARENT_DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION_CAPITAL;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID_CAPITAL;
import static org.cmdbuild.servlets.json.CommunicationConstants.PARENT_DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.PARENT_ID;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.translation.LookupTranslation;
import org.json.JSONException;
import org.json.JSONObject;

public class LookupSerializer {

	private final static LookupStore lookupStore = applicationContext().getBean(LookupStore.class);
	private final TranslationFacade translationFacade;

	public LookupSerializer(final TranslationFacade translationFacade) {
		this.translationFacade = translationFacade;
	}

	public JSONObject serializeLookup(final Lookup lookup) throws JSONException {
		return serializeLookup(lookup, false);
	}

	public JSONObject serializeLookup(final Lookup lookup, final boolean shortForm) throws JSONException {
		JSONObject serializer = null;
		if (lookup != null) {
			serializer = new JSONObject();
			serializer.put(ID_CAPITAL, lookup.getId());

			final LookupTranslation translationObject = LookupTranslation.newInstance() //
					.withField(DESCRIPTION_FOR_CLIENT) //
					.withName(lookup.getTranslationUuid()) //
					.build();

			final String translatedDescription = translationFacade.read(translationObject);

			serializer.put(DESCRIPTION_CAPITAL, defaultIfNull(translatedDescription, lookup.description));
			serializer.put(DEFAULT_DESCRIPTION_CAPITAL, lookup.description);

			if (!shortForm) {
				serializer.put("Type", lookup.type.name);
				serializer.put("Code", defaultIfEmpty(lookup.code, EMPTY));
				serializer.put("Number", lookup.number);
				serializer.put("Notes", lookup.notes);
				serializer.put("Default", lookup.isDefault);
				serializer.put("Active", lookup.active);

				serializer.put("TranslationUuid", lookup.translationUuid);
			}

			final Lookup parent = lookup.parent;
			if (parent != null) {
				serializer.put("ParentId", parent.getId());
				if (!shortForm) {

					final LookupTranslation parentTranslationObject = LookupTranslation.newInstance() //
							.withField(DESCRIPTION_FOR_CLIENT) //
							.withName(parent.getId() != null ? parent.getId().toString() : EMPTY) //
							.build();

					final String parentTranslatedDescription = translationFacade.read(parentTranslationObject);
					serializer.put(PARENT_DESCRIPTION, defaultIfNull(parentTranslatedDescription, parent.description));
					serializer.put(DEFAULT_PARENT_DESCRIPTION,
							defaultIfNull(parentTranslatedDescription, parent.description));
					serializer.put("ParentType", parent.type);
				}
			}
		}
		return serializer;
	}

	public JSONObject serializeLookupParent(final Lookup lookup) throws JSONException {
		JSONObject serializer = null;
		if (lookup != null) {

			final LookupTranslation parentTranslationObject = LookupTranslation.newInstance() //
					.withField(DESCRIPTION_FOR_CLIENT) //
					.withName(lookup.getId() != null ? lookup.getId().toString() : EMPTY) //
					.build();

			final String parentTranslatedDescription = translationFacade.read(parentTranslationObject);

			serializer = new JSONObject();
			serializer.put(PARENT_ID, lookup.getId());
			serializer.put(PARENT_DESCRIPTION, defaultIfNull(parentTranslatedDescription, lookup.description));
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

		final Map<String, Object> out = new HashMap<String, Object>();
		out.put(ID, value.getId());
		out.put(DESCRIPTION, description(value));
		return out;
	}

	private String description(final LookupValue value) {
		String description = value.getDescription();

		final LookupTranslation lookupTranslation = LookupTranslation.newInstance() //
				.withField(DESCRIPTION_FOR_CLIENT)//
				.withName(value.getId() != null ? value.getId().toString() : EMPTY)//
				// TODO
				// .withName(value.getTranslationUuid() != null ?
				// value.getTranslationUuid() : EMPTY)//
				.build();

		final String translatedDescription = translationFacade.read(lookupTranslation);
		final String baseDescription = defaultIfNull(translatedDescription, description);

		if (value instanceof LookupValue) {
			Lookup lookup = lookup(value.getId());
			if (lookup != null) {
				lookup = lookup(lookup.parentId);
				while (lookup != null) {
					final LookupTranslation parentTranslation = LookupTranslation.newInstance() //
							.withField(DESCRIPTION_FOR_CLIENT)//
							.withName(lookup.getId() != null ? value.getId().toString() : EMPTY)//
							.build();
					final String parentTranslatedDescription = translationFacade.read(parentTranslation);

					final String format = "%s - %s";
					description = String.format(format, defaultIfNull(parentTranslatedDescription, lookup.description),
							baseDescription);
					lookup = lookup(lookup.parentId);
				}
			}
		}
		return baseDescription;
	}

	private static Lookup lookup(final Long id) {
		if (id != null) {
			return lookupStore.read(new Storable() {
				@Override
				public String getIdentifier() {
					return id.toString();
				}
			});
		} else {
			return null;
		}
	}
}
