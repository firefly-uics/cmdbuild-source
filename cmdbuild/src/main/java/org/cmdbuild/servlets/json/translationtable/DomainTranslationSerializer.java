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

import com.google.common.collect.Lists;

public class DomainTranslationSerializer extends EntryTypeTranslationSerializer {

	public DomainTranslationSerializer(final DataAccessLogic dataLogic, final boolean activeOnly,
			final TranslationLogic translationLogic) {
		super(dataLogic, activeOnly, translationLogic);
	}

	@Override
	public JsonResponse serialize() {

		final Iterable<? extends CMDomain> allDomains = activeOnly ? dataLogic.findActiveDomains() : dataLogic
				.findAllDomains();
		final Iterable<? extends CMDomain> sortedDomains = EntryTypeSorter //
				.of(ENTRYTYPE_SORTER_PROPERTY) //
				.getOrdering(ENTRYTYPE_SORTER_DIRECTION) //
				.sortedCopy(allDomains);

		final Collection<JsonElement> jsonDomains = Lists.newArrayList();
		for (final CMDomain domain : sortedDomains) {
			final String domainName = domain.getName();
			final Collection<JsonField> jsonFields = readFields(domain);
			final Iterable<? extends CMAttribute> allAttributes = domain.getAllAttributes();
			final Iterable<? extends CMAttribute> sortedAttributes = sortAttributes(allAttributes);
			final Collection<JsonElement> jsonAttributes = serializeAttributes(sortedAttributes);
			final JsonElement jsonDomain = new JsonElement();
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
