package org.cmdbuild.servlets.json.schema;

import static org.cmdbuild.servlets.json.CommunicationConstants.ATTRIBUTENAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.CHARTNAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.DASHBOARDNAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.FIELD;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTERNAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.ICONNAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.MENU_ITEM_UUID;
import static org.cmdbuild.servlets.json.CommunicationConstants.REPORTNAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.TRANSLATIONS;
import static org.cmdbuild.servlets.json.CommunicationConstants.TRANSLATION_UUID;
import static org.cmdbuild.servlets.json.CommunicationConstants.VIEWNAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.WIDGET_ID;
import static org.cmdbuild.servlets.json.schema.Utils.toMap;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.logic.translation.ChartTranslation;
import org.cmdbuild.logic.translation.DashboardTranslation;
import org.cmdbuild.logic.translation.FilterTranslation;
import org.cmdbuild.logic.translation.GisIconTranslation;
import org.cmdbuild.logic.translation.InstanceNameTranslation;
import org.cmdbuild.logic.translation.LookupTranslation;
import org.cmdbuild.logic.translation.MenuItemTranslation;
import org.cmdbuild.logic.translation.ReportTranslation;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.ViewTranslation;
import org.cmdbuild.logic.translation.WidgetTranslation;
import org.cmdbuild.logic.translation.converter.AttributeConverter;
import org.cmdbuild.logic.translation.converter.ClassConverter;
import org.cmdbuild.logic.translation.converter.DomainConverter;
import org.cmdbuild.services.CustomFilesStore;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONObject;

public class Translation extends JSONBaseWithSpringContext {

	// FIXME get rid of this
	private static final FilesStore iconsFileStore = new CustomFilesStore();

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

		final AttributeConverter converter = AttributeConverter.of(AttributeConverter.aClass(), field);
		Validate.isTrue(converter.isValid());
		converter.withTranslations(toMap(translations));
		final TranslationObject translationObject = converter.create(className, attributeName);
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
		converter.withTranslations(toMap(translations));
		final TranslationObject translationObject = converter.create(domainName);
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
		final AttributeConverter converter = AttributeConverter.of(AttributeConverter.aDomain(), FIELD);
		Validate.isTrue(converter.isValid());
		converter.withTranslations(toMap(translations));
		final TranslationObject translationObject = converter.create(domainName, attributeName);
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public void createForView( //
			@Parameter(value = VIEWNAME) final String viewName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ViewTranslation translationObject = ViewTranslation.newInstance() //
				.withName(viewName) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public void createForFilter( //
			@Parameter(value = FILTERNAME) final String filterName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final FilterTranslation translationObject = FilterTranslation.newInstance() //
				.withField(field) //
				.withName(filterName) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public void createForInstanceName( //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final InstanceNameTranslation translationObject = new InstanceNameTranslation();
		translationObject.setTranslations(toMap(translations));
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public void createForMenuItem( //
			@Parameter(value = MENU_ITEM_UUID) final String itemUniqueIdentifier, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final MenuItemTranslation translationObject = MenuItemTranslation.newInstance() //
				.withName(itemUniqueIdentifier) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public void createForWidget( //
			@Parameter(value = WIDGET_ID) final String widgetId, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final WidgetTranslation translationObject = WidgetTranslation.newInstance() //
				.withName(widgetId) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public void createForDashboard( //
			@Parameter(value = DASHBOARDNAME) final String dashboardName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final DashboardTranslation translationObject = new DashboardTranslation();
		translationObject.setName(dashboardName);
		translationObject.setField(field);
		translationObject.setTranslations(toMap(translations));
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public void createForChart( //
			@Parameter(value = CHARTNAME) final String chartName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ChartTranslation translationObject = new ChartTranslation();
		translationObject.setName(chartName);
		translationObject.setField(field);
		translationObject.setTranslations(toMap(translations));
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public void createForReport( //
			@Parameter(value = REPORTNAME) final String reportName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ReportTranslation translationObject = ReportTranslation.newInstance() //
				.withField(field) //
				.withName(reportName) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public void createForLookup( //
			@Parameter(value = TRANSLATION_UUID) final String translationUuid, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final LookupTranslation translationObject = LookupTranslation.newInstance() //
				.withName(translationUuid) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().create(translationObject);
	}

	@JSONExported
	@Admin
	public void createForGisIcon( //
			@Parameter(value = ICONNAME) final String iconName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final String description = iconsFileStore.removeExtension(iconName);
		final GisIconTranslation translationObject = GisIconTranslation.newInstance() //
				.withName(description) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
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
		final AttributeConverter converter = AttributeConverter.of(AttributeConverter.aClass(), field);
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
		final AttributeConverter converter = AttributeConverter.of(AttributeConverter.aDomain(), field);
		Validate.isTrue(converter.isValid());
		final TranslationObject translationObject = converter.create(domainName, attributeName);
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForView( //
			@Parameter(value = VIEWNAME) final String viewName, //
			@Parameter(value = FIELD) final String field) {
		final ViewTranslation translationObject = ViewTranslation.newInstance() //
				.withName(viewName) //
				.withField(field) //
				.build();
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForFilter( //
			@Parameter(value = FILTERNAME) final String filterName, //
			@Parameter(value = FIELD) final String field) {
		final FilterTranslation translationObject = FilterTranslation.newInstance() //
				.withField(field) //
				.withName(filterName) //
				.build();
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForInstanceName() {
		final InstanceNameTranslation translationObject = new InstanceNameTranslation();
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForWidget( //
			@Parameter(value = WIDGET_ID) final String widgetId, //
			@Parameter(value = FIELD) final String field //
	) {
		final WidgetTranslation translationObject = WidgetTranslation.newInstance() //
				.withName(widgetId) //
				.withField(field) //
				.build();
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForDashboard( //
			@Parameter(value = DASHBOARDNAME) final String dashboardName, //
			@Parameter(value = FIELD) final String field //
	) {
		final DashboardTranslation translationObject = new DashboardTranslation();
		translationObject.setName(dashboardName);
		translationObject.setField(field);
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForChart( //
			@Parameter(value = CHARTNAME) final String chartName, //
			@Parameter(value = FIELD) final String field //
	) {
		final ChartTranslation translationObject = new ChartTranslation();
		translationObject.setName(chartName);
		translationObject.setField(field);
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForReport( //
			@Parameter(value = REPORTNAME) final String reportName, //
			@Parameter(value = FIELD) final String field //
	) {
		final ReportTranslation translationObject = ReportTranslation.newInstance() //
				.withField(field) //
				.withName(reportName) //
				.build();
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForLookup( //
			@Parameter(value = TRANSLATION_UUID) final String lookupId, //
			@Parameter(value = FIELD) final String field //
	) {
		final LookupTranslation translationObject = LookupTranslation.newInstance() //
				.withName(lookupId) //
				.withField(field) //
				.build();
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForMenuItem( //
			@Parameter(value = MENU_ITEM_UUID) final String uniqueIdentifier, //
			@Parameter(value = FIELD) final String field //
	) {
		final MenuItemTranslation translationObject = MenuItemTranslation.newInstance() //
				.withName(uniqueIdentifier) //
				.withField(field) //
				.build();
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForGisIcon( //
			@Parameter(value = ICONNAME) final String iconName, //
			@Parameter(value = FIELD) final String field //
	) {
		final String description = iconsFileStore.removeExtension(iconName);
		final GisIconTranslation translationObject = GisIconTranslation.newInstance() //
				.withName(description) //
				.withField(field) //
				.build();
		translationLogic().create(translationObject);
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
		final AttributeConverter converter = AttributeConverter.of(AttributeConverter.aClass(), field);
		converter.withTranslations(toMap(translations));
		final TranslationObject translationObject = converter.create(className, attributeName);
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
		converter.withTranslations(toMap(translations));
		final TranslationObject translationObject = converter.create(domainName);
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
//		final DomainTranslation translationObject = DomainTranslation.newInstance() //
//				.withName(domainName) //
//				.withField(field) //
//				.withTranslations(toMap(translations)) //
//				.build();
//		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public void updateForView( //
			@Parameter(value = VIEWNAME) final String viewName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ViewTranslation translationObject = ViewTranslation.newInstance() //
				.withName(viewName) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public void updateForFilter( //
			@Parameter(value = FILTERNAME) final String filterName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final FilterTranslation translationObject = FilterTranslation.newInstance() //
				.withField(field) //
				.withName(filterName) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public void updateForInstanceName( //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final InstanceNameTranslation translationObject = new InstanceNameTranslation();
		translationObject.setTranslations(toMap(translations));
		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public void updateForWidget( //
			@Parameter(value = WIDGET_ID) final String widgetId, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final WidgetTranslation translationObject = WidgetTranslation.newInstance() //
				.withName(widgetId) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public void updateForDashboard( //
			@Parameter(value = DASHBOARDNAME) final String dashboardName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final DashboardTranslation translationObject = new DashboardTranslation();
		translationObject.setName(dashboardName);
		translationObject.setField(field);
		translationObject.setTranslations(toMap(translations));
		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public void updateForChart( //
			@Parameter(value = CHARTNAME) final String chartName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ChartTranslation translationObject = new ChartTranslation();
		translationObject.setName(chartName);
		translationObject.setField(field);
		translationObject.setTranslations(toMap(translations));
		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public void updateForReport( //
			@Parameter(value = REPORTNAME) final String reportName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ReportTranslation translationObject = ReportTranslation.newInstance() //
				.withField(field) //
				.withName(reportName) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public void updateForLookup( //
			@Parameter(value = TRANSLATION_UUID) final String lookupId, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final LookupTranslation translationObject = LookupTranslation.newInstance() //
				.withName(lookupId) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public void updateForMenuItem( //
			@Parameter(value = MENU_ITEM_UUID) final String uniqueIdentifier, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final MenuItemTranslation translationObject = MenuItemTranslation.newInstance() //
				.withName(uniqueIdentifier) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public void updateForGisIcon( //
			@Parameter(value = ICONNAME) final String iconName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final String description = iconsFileStore.removeExtension(iconName);
		final GisIconTranslation translationObject = GisIconTranslation.newInstance() //
				.withName(description) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().update(translationObject);
	}

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
		final AttributeConverter converter = AttributeConverter.of(AttributeConverter.aClass(), field);
		Validate.isTrue(converter.isValid());
		converter.withTranslations(toMap(translations));
		final TranslationObject translationObject = converter.create(className, attributeName);
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
		converter.withTranslations(toMap(translations));
		final TranslationObject translationObject = converter.create(domainName);
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
		final AttributeConverter converter = AttributeConverter.of(AttributeConverter.aDomain(), field);
		Validate.isTrue(converter.isValid());
		converter.withTranslations(toMap(translations));
		final TranslationObject translationObject = converter.create(domainName, attributeName);
		translationLogic().delete(translationObject);
	}

	@JSONExported
	@Admin
	public void deleteForView( //
			@Parameter(value = VIEWNAME) final String viewName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ViewTranslation translationObject = ViewTranslation.newInstance() //
				.withName(viewName) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().delete(translationObject);
	}

	@JSONExported
	@Admin
	public void deleteForFilter( //
			@Parameter(value = FILTERNAME) final String filterName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final FilterTranslation translationObject = FilterTranslation.newInstance() //
				.withField(field) //
				.withName(filterName) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().delete(translationObject);
	}

	@JSONExported
	@Admin
	public void deleteForInstanceName( //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final InstanceNameTranslation translationObject = new InstanceNameTranslation();
		translationObject.setTranslations(toMap(translations));
		translationLogic().delete(translationObject);
	}

	@JSONExported
	@Admin
	public void deleteForWidget( //
			@Parameter(value = WIDGET_ID) final String widgetId, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final WidgetTranslation translationObject = WidgetTranslation.newInstance() //
				.withName(widgetId) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().delete(translationObject);
	}

	@JSONExported
	@Admin
	public void deleteForDashboard( //
			@Parameter(value = DASHBOARDNAME) final String dashboardName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final DashboardTranslation translationObject = new DashboardTranslation();
		translationObject.setName(dashboardName);
		translationObject.setField(field);
		translationObject.setTranslations(toMap(translations));
		translationLogic().delete(translationObject);
	}

	@JSONExported
	@Admin
	public void deleteForChart( //
			@Parameter(value = CHARTNAME) final String chartName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ChartTranslation translationObject = new ChartTranslation();
		translationObject.setName(chartName);
		translationObject.setField(field);
		translationObject.setTranslations(toMap(translations));
		translationLogic().delete(translationObject);
	}

	@JSONExported
	@Admin
	public void deleteForReport( //
			@Parameter(value = REPORTNAME) final String reportName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ReportTranslation translationObject = ReportTranslation.newInstance() //
				.withField(field) //
				.withName(reportName) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().delete(translationObject);
	}

	@JSONExported
	@Admin
	public void deleteForLookup( //
			@Parameter(value = TRANSLATION_UUID) final String lookupId, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final LookupTranslation translationObject = LookupTranslation.newInstance() //
				.withName(lookupId) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().delete(translationObject);
	}

	@JSONExported
	@Admin
	public void deleteForMenuItem( //
			@Parameter(value = MENU_ITEM_UUID) final String itemUniqueIdentifier, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final MenuItemTranslation translationObject = MenuItemTranslation.newInstance() //
				.withName(itemUniqueIdentifier) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().delete(translationObject);
	}

	@JSONExported
	@Admin
	public void deleteForGisIcon( //
			@Parameter(value = ICONNAME) final String iconName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {

		final String description = iconsFileStore.removeExtension(iconName);
		final GisIconTranslation translationObject = GisIconTranslation.newInstance() //
				.withName(description) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().delete(translationObject);
	}

}