package org.cmdbuild.servlets.json.translationtable;

import java.util.Collection;
import java.util.Map;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.ClassConverter;
import org.cmdbuild.logic.translation.converter.DomainConverter;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.translationtable.objects.JsonElement;
import org.cmdbuild.servlets.json.translationtable.objects.JsonElementWithAttributes;
import org.cmdbuild.servlets.json.translationtable.objects.JsonField;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class DomainTranslationSerializer extends EntryTypeTranslationSerializer {

	DomainTranslationSerializer(final DataAccessLogic dataLogic, final boolean activeOnly,
			final TranslationLogic translationLogic, final JSONArray sorters) {
		super(dataLogic, activeOnly, translationLogic);
		setOrderings(sorters);
	}

	private void setOrderings(final JSONArray sorters) {
		if (sorters != null) {
			try {
				for (int i = 0; i < sorters.length(); i++) {
					final JSONObject object = JSONObject.class.cast(sorters.get(i));
					final String element = object.getString(ELEMENT);
					if (element.equalsIgnoreCase(DOMAIN)) {
						entryTypeOrdering = EntryTypeSorter.of(object.getString(FIELD)) //
								.withDirection(object.getString(DIRECTION)) //
								.getOrientedOrdering();
					} else if (element.equalsIgnoreCase(ATTRIBUTE)) {
						attributeOrdering = AttributeSorter.of(object.getString(FIELD)) //
								.withDirection(object.getString(DIRECTION)) //
								.getOrientedOrdering();
					}
				}
			} catch (final JSONException e) {
				// nothing to do
			}
		}
	}

	@Override
	public JsonResponse serialize() {
		final Iterable<? extends CMDomain> allDomains = activeOnly ? dataLogic.findActiveDomains() : dataLogic
				.findAllDomains();
		final Iterable<? extends CMDomain> sortedDomains = entryTypeOrdering.sortedCopy(allDomains);

		final Collection<JsonElementWithAttributes> jsonDomains = Lists.newArrayList();
		for (final CMDomain domain : sortedDomains) {
			final String domainName = domain.getName();
			final Collection<JsonField> jsonFields = readFields(domain);
			final Iterable<? extends CMAttribute> allAttributes = domain.getAllAttributes();
			final Iterable<? extends CMAttribute> sortedAttributes = sortAttributes(allAttributes);
			final Collection<JsonElement> jsonAttributes = serializeAttributes(sortedAttributes);
			final JsonElementWithAttributes jsonDomain = new JsonElementWithAttributes();
			jsonDomain.setName(domainName);
			jsonDomain.setAttributes(jsonAttributes);
			jsonDomain.setFields(jsonFields);
			jsonDomains.add(jsonDomain);
		}
		return JsonResponse.success(jsonDomains);
	}

	private Collection<JsonField> readFields(final CMDomain domain) {
		final Collection<JsonField> jsonFields = Lists.newArrayList();
		final TranslationObject descriptionTranslationObject = DomainConverter.DESCRIPTION //
				.withIdentifier(domain.getName()) //
				.create();
		final Map<String, String> descriptionTranslations = translationLogic //
				.readAll(descriptionTranslationObject);
		final JsonField descriptionField = new JsonField();
		descriptionField.setName(ClassConverter.description());
		descriptionField.setTranslations(descriptionTranslations);
		descriptionField.setValue(domain.getDescription());
		jsonFields.add(descriptionField);

		final TranslationObject directDescriptionTranslationObject = DomainConverter.DIRECT_DESCRIPTION //
				.withIdentifier(domain.getName()) //
				.create();
		final Map<String, String> directDescriptionTranslations = translationLogic //
				.readAll(directDescriptionTranslationObject);
		final JsonField directDescriptionField = new JsonField();
		directDescriptionField.setName(DomainConverter.directDescription());
		directDescriptionField.setTranslations(directDescriptionTranslations);
		directDescriptionField.setValue(domain.getDescription1());
		jsonFields.add(directDescriptionField);

		final TranslationObject inverseDescriptionTranslationObject = DomainConverter.INVERSE_DESCRIPTION //
				.withIdentifier(domain.getName()) //
				.create();
		final Map<String, String> inverseDescriptionTranslations = translationLogic //
				.readAll(inverseDescriptionTranslationObject);
		final JsonField inverseDescriptionField = new JsonField();
		inverseDescriptionField.setName(DomainConverter.inverseDescription());
		inverseDescriptionField.setTranslations(inverseDescriptionTranslations);
		inverseDescriptionField.setValue(domain.getDescription2());
		jsonFields.add(inverseDescriptionField);

		return jsonFields;
	}

}
