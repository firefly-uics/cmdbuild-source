package org.cmdbuild.servlets.json.schema;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.servlets.json.CommunicationConstants.ATTRIBUTENAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.FIELD;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTERNAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.MENU_ITEM_UUID;
import static org.cmdbuild.servlets.json.CommunicationConstants.REPORTNAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.TRANSLATIONS;
import static org.cmdbuild.servlets.json.CommunicationConstants.TRANSLATION_UUID;
import static org.cmdbuild.servlets.json.CommunicationConstants.VIEWNAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.WIDGET_ID;
import static org.cmdbuild.servlets.json.schema.Utils.toMap;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.AttributeConverter;
import org.cmdbuild.logic.translation.converter.ClassConverter;
import org.cmdbuild.logic.translation.converter.DomainConverter;
import org.cmdbuild.logic.translation.converter.FilterConverter;
import org.cmdbuild.logic.translation.converter.InstanceConverter;
import org.cmdbuild.logic.translation.converter.LookupConverter;
import org.cmdbuild.logic.translation.converter.MenuItemConverter;
import org.cmdbuild.logic.translation.converter.ReportConverter;
import org.cmdbuild.logic.translation.converter.ViewConverter;
import org.cmdbuild.logic.translation.converter.WidgetConverter;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONObject;

public class Translation extends JSONBaseWithSpringContext {

	@JSONExported
	@Admin
	public void createForClass( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ClassConverter converter = ClassConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(className);
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public void createForClassAttribute( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = ATTRIBUTENAME) final String attributeName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {

		final AttributeConverter converter = AttributeConverter.of(AttributeConverter.forClass(), field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(className, attributeName);
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public void createForDomain( //
			@Parameter(value = DOMAIN_NAME) final String domainName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final DomainConverter converter = DomainConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(domainName);
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public void createForDomainAttribute( //
			@Parameter(value = DOMAIN_NAME) final String domainName, //
			@Parameter(value = ATTRIBUTENAME) final String attributeName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final AttributeConverter converter = AttributeConverter.of(AttributeConverter.forDomain(), field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(domainName, attributeName);
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public void createForFilter( //
			@Parameter(value = FILTERNAME) final String filterName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final FilterConverter converter = FilterConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(filterName);
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public void createForInstanceName( //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final InstanceConverter converter = InstanceConverter.of(InstanceConverter.nameField());
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)).create(EMPTY);
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public void createForLookup( //
			@Parameter(value = TRANSLATION_UUID) final String uuid, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final LookupConverter converter = LookupConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)).create(uuid);
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public void createForMenuItem( //
			@Parameter(value = MENU_ITEM_UUID) final String uuid, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final MenuItemConverter converter = MenuItemConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(uuid);
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public void createForReport( //
			@Parameter(value = REPORTNAME) final String reportName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ReportConverter converter = ReportConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(reportName);
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public void createForView( //
			@Parameter(value = VIEWNAME) final String viewName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ViewConverter converter = ViewConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(viewName);
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public void createForWidget( //
			@Parameter(value = WIDGET_ID) final String widgetId, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final WidgetConverter converter = WidgetConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(widgetId);
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public JsonResponse readForClass( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = FIELD) final String field //
	) {
		final ClassConverter converter = ClassConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter.create(className);
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForClassAttribute( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = ATTRIBUTENAME) final String attributeName, //
			@Parameter(value = FIELD) final String field //
	) {
		final AttributeConverter converter = AttributeConverter.of(AttributeConverter.forClass(), field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter.create(className, attributeName);
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForDomain( //
			@Parameter(value = DOMAIN_NAME) final String domainName, //
			@Parameter(value = FIELD) final String field //
	) {
		final DomainConverter converter = DomainConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter.create(domainName);
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForDomainAttribute( //
			@Parameter(value = DOMAIN_NAME) final String domainName, //
			@Parameter(value = ATTRIBUTENAME) final String attributeName, //
			@Parameter(value = FIELD) final String field //
	) {
		final AttributeConverter converter = AttributeConverter.of(AttributeConverter.forDomain(), field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter.create(domainName, attributeName);
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForFilter( //
			@Parameter(value = FILTERNAME) final String filterName, //
			@Parameter(value = FIELD) final String field) {
		final FilterConverter converter = FilterConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter.create(filterName);
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForInstanceName() {
		final InstanceConverter converter = InstanceConverter.of(InstanceConverter.nameField());
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter.create(EMPTY);
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForLookup( //
			@Parameter(value = TRANSLATION_UUID) final String uuid, //
			@Parameter(value = FIELD) final String field //
	) {
		final LookupConverter converter = LookupConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter.create(uuid);
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForMenuItem( //
			@Parameter(value = MENU_ITEM_UUID) final String uuid, //
			@Parameter(value = FIELD) final String field //
	) {
		final MenuItemConverter converter = MenuItemConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.create(uuid);
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForReport( //
			@Parameter(value = REPORTNAME) final String reportName, //
			@Parameter(value = FIELD) final String field //
	) {
		final ReportConverter converter = ReportConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.create(reportName);
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForView( //
			@Parameter(value = VIEWNAME) final String viewName, //
			@Parameter(value = FIELD) final String field) {
		final ViewConverter converter = ViewConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.create(viewName);
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForWidget( //
			@Parameter(value = WIDGET_ID) final String widgetId, //
			@Parameter(value = FIELD) final String field //
	) {
		final WidgetConverter converter = WidgetConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.create(widgetId);
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	/*
	 * Translations: UPDATE
	 */
	@JSONExported
	@Admin
	public void updateForClass( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ClassConverter converter = ClassConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(className);
		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public void updateForClassAttribute( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = ATTRIBUTENAME) final String attributeName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final AttributeConverter converter = AttributeConverter.of(AttributeConverter.forClass(), field);
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(className, attributeName);
		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public void updateForDomain( //
			@Parameter(value = DOMAIN_NAME) final String domainName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final DomainConverter converter = DomainConverter.of(field);
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(domainName);
		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public void updateForDomainAttribute( //
			@Parameter(value = DOMAIN_NAME) final String domainName, //
			@Parameter(value = ATTRIBUTENAME) final String attributeName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final AttributeConverter converter = AttributeConverter.of(AttributeConverter.forDomain(), field);
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(domainName, attributeName);
		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public void updateForFilter( //
			@Parameter(value = FILTERNAME) final String filterName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final FilterConverter converter = FilterConverter.of(field);
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(filterName);
		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public void updateForInstanceName( //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final InstanceConverter converter = InstanceConverter.of(InstanceConverter.nameField());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(EMPTY);
		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public void updateForLookup( //
			@Parameter(value = TRANSLATION_UUID) final String uuid, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final LookupConverter converter = LookupConverter.of(field);
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(uuid);
		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public void updateForMenuItem( //
			@Parameter(value = MENU_ITEM_UUID) final String uuid, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final MenuItemConverter converter = MenuItemConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(uuid);
		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public void updateForReport( //
			@Parameter(value = REPORTNAME) final String reportName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ReportConverter converter = ReportConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(reportName);
		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public void updateForView( //
			@Parameter(value = VIEWNAME) final String viewName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ViewConverter converter = ViewConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(viewName);
		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public void updateForWidget( //
			@Parameter(value = WIDGET_ID) final String widgetId, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final WidgetConverter converter = WidgetConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(widgetId);
		translationLogic().update(translationObject);
	}

	/*
	 * Translations: DELETE
	 */

	@JSONExported
	@Admin
	public void deleteForClass( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ClassConverter converter = ClassConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(className);
		translationLogic().delete(translationObject);
	}

	@JSONExported
	@Admin
	public void deleteForClassAttribute( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = ATTRIBUTENAME) final String attributeName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final AttributeConverter converter = AttributeConverter.of(AttributeConverter.forClass(), field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(className, attributeName);
		translationLogic().delete(translationObject);
	}

	@JSONExported
	@Admin
	public void deleteForDomain( //
			@Parameter(value = DOMAIN_NAME) final String domainName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final DomainConverter converter = DomainConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(domainName);
		translationLogic().delete(translationObject);
	}

	@JSONExported
	@Admin
	public void deleteForDomainAttribute( //
			@Parameter(value = DOMAIN_NAME) final String domainName, //
			@Parameter(value = ATTRIBUTENAME) final String attributeName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final AttributeConverter converter = AttributeConverter.of(AttributeConverter.forDomain(), field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(domainName, attributeName);
		translationLogic().delete(translationObject);
	}

	@JSONExported
	@Admin
	public void deleteForFilter( //
			@Parameter(value = FILTERNAME) final String filterName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final FilterConverter converter = FilterConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(filterName);
		translationLogic().delete(translationObject);
	}

	@JSONExported
	@Admin
	public void deleteForInstanceName( //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final InstanceConverter converter = InstanceConverter.of(InstanceConverter.nameField());
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(EMPTY);
		translationLogic().delete(translationObject);
	}

	@JSONExported
	@Admin
	public void deleteForLookup( //
			@Parameter(value = TRANSLATION_UUID) final String uuid, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final LookupConverter converter = LookupConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(uuid);
		translationLogic().delete(translationObject);
	}

	@JSONExported
	@Admin
	public void deleteForMenuItem( //
			@Parameter(value = MENU_ITEM_UUID) final String uuid, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final MenuItemConverter converter = MenuItemConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(uuid);
		translationLogic().delete(translationObject);
	}

	@JSONExported
	@Admin
	public void deleteForReport( //
			@Parameter(value = REPORTNAME) final String reportName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ReportConverter converter = ReportConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(reportName);
		translationLogic().delete(translationObject);
	}

	@JSONExported
	@Admin
	public void deleteForView( //
			@Parameter(value = VIEWNAME) final String viewName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ViewConverter converter = ViewConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(viewName);
		translationLogic().delete(translationObject);
	}

	@JSONExported
	@Admin
	public void deleteForWidget( //
			@Parameter(value = WIDGET_ID) final String widgetId, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final WidgetConverter converter = WidgetConverter.of(field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter //
				.withTranslations(toMap(translations)) //
				.create(widgetId);
		translationLogic().delete(translationObject);
	}
}