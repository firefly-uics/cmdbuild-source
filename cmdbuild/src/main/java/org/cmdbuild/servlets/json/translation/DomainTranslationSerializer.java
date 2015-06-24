package org.cmdbuild.servlets.json.translation;

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
import org.cmdbuild.servlets.json.schema.Translation.JsonAttribute;
import org.cmdbuild.servlets.json.schema.Translation.JsonEntryType;
import org.cmdbuild.servlets.json.schema.Translation.JsonField;

import com.google.common.collect.Lists;

public class DomainTranslationSerializer extends ClassTranslationSerializer {

	public DomainTranslationSerializer(final DataAccessLogic dataLogic, final boolean activeOnly,
			final TranslationLogic translationLogic) {
		super(dataLogic, activeOnly, translationLogic);
	}

	@Override
	public JsonResponse serialize() {
		final Iterable<? extends CMDomain> allDomains = dataLogic.findAllDomains();
		final Collection<JsonEntryType> jsonDomains = Lists.newArrayList();
		for (final CMDomain domain : allDomains) {
			final String className = domain.getName();
			Collection<JsonField> jsonFields = readFields(domain);
			final Iterable<? extends CMAttribute> allAttributes = domain.getAllAttributes();
			final Collection<JsonAttribute> jsonAttributes = Lists.newArrayList();
			for (final CMAttribute attribute : allAttributes) {
				final String attributeName = attribute.getName();
				jsonFields = readFields(attribute);
				final JsonAttribute jsonAttribute = new JsonAttribute();
				jsonAttribute.setName(attributeName);
				jsonAttribute.setFields(jsonFields);
				jsonAttributes.add(jsonAttribute);
			}
			final JsonEntryType jsonDomain = new JsonEntryType();
			jsonDomain.setName(className);
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
