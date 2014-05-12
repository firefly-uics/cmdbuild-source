package org.cmdbuild.servlets.json.schema;

import static org.cmdbuild.servlets.json.ComunicationConstants.ATTRIBUTENAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.CHARTNAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.DASHBOARDNAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAIN_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.FIELD;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILTERNAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.ICONNAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.LOOKUPID;
import static org.cmdbuild.servlets.json.ComunicationConstants.REPORTNAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.TRANSLATIONS;
import static org.cmdbuild.servlets.json.ComunicationConstants.VIEWNAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.WIDGET_ID;
import static org.cmdbuild.servlets.json.schema.Utils.toMap;

import java.util.Map;

import org.cmdbuild.logic.translation.AttributeClassTranslation;
import org.cmdbuild.logic.translation.AttributeDomainTranslation;
import org.cmdbuild.logic.translation.ChartTranslation;
import org.cmdbuild.logic.translation.ClassTranslation;
import org.cmdbuild.logic.translation.DashboardTranslation;
import org.cmdbuild.logic.translation.DomainTranslation;
import org.cmdbuild.logic.translation.FilterTranslation;
import org.cmdbuild.logic.translation.FilterViewTranslation;
import org.cmdbuild.logic.translation.GisIconTranslation;
import org.cmdbuild.logic.translation.InstanceNameTranslation;
import org.cmdbuild.logic.translation.LookupTranslation;
import org.cmdbuild.logic.translation.ReportTranslation;
import org.cmdbuild.logic.translation.SqlViewTranslation;
import org.cmdbuild.logic.translation.WidgetTranslation;
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
		final ClassTranslation translation = ClassTranslation.newInstance() //
				.withName(className) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().create(translation);
	}

	@JSONExported
	@Admin
	public void createForClassAttribute( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = ATTRIBUTENAME) final String attributeName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final AttributeClassTranslation translation = AttributeClassTranslation.newInstance() //
				.forClass(className) //
				.withField(field) //
				.withName(attributeName) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().create(translation);
	}

	@JSONExported
	@Admin
	public void createForDomain( //
			@Parameter(value = DOMAIN_NAME) final String domainName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final DomainTranslation translation = DomainTranslation.newInstance() //
				.withName(domainName) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().create(translation);
	}

	@JSONExported
	@Admin
	public void createForDomainAttribute( //
			@Parameter(value = DOMAIN_NAME) final String domainName, //
			@Parameter(value = ATTRIBUTENAME) final String attributeName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final AttributeDomainTranslation translation = new AttributeDomainTranslation();
		translation.setName(domainName);
		translation.setAttributeName(attributeName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().create(translation);

	}

	@JSONExported
	@Admin
	public void createForFilterView( //
			@Parameter(value = VIEWNAME) final String viewName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final FilterViewTranslation translation = new FilterViewTranslation();
		translation.setName(viewName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().create(translation);

	}

	@JSONExported
	@Admin
	public void createForSqlView( //
			@Parameter(value = VIEWNAME) final String viewName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final SqlViewTranslation translation = new SqlViewTranslation();
		translation.setName(viewName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().create(translation);

	}

	@JSONExported
	@Admin
	public void createForFilter( //
			@Parameter(value = FILTERNAME) final String filterName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final FilterTranslation translation = new FilterTranslation();
		translation.setName(filterName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().create(translation);

	}

	@JSONExported
	@Admin
	public void createForInstanceName( //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final InstanceNameTranslation translation = new InstanceNameTranslation();
		translation.setTranslations(toMap(translations));
		translationLogic().create(translation);

	}

	@JSONExported
	@Admin
	public void createForWidget( //
			@Parameter(value = WIDGET_ID) final String widgetId, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final WidgetTranslation translation = new WidgetTranslation();
		translation.setName(widgetId);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().create(translation);

	}

	@JSONExported
	@Admin
	public void createForDashboard( //
			@Parameter(value = DASHBOARDNAME) final String dashboardName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final DashboardTranslation translation = new DashboardTranslation();
		translation.setName(dashboardName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().create(translation);

	}

	@JSONExported
	@Admin
	public void createForChart( //
			@Parameter(value = CHARTNAME) final String chartName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ChartTranslation translation = new ChartTranslation();
		translation.setName(chartName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().create(translation);

	}

	@JSONExported
	@Admin
	public void createForReport( //
			@Parameter(value = REPORTNAME) final String reportName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ReportTranslation translation = new ReportTranslation();
		translation.setName(reportName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().create(translation);

	}

	@JSONExported
	@Admin
	public void createForLookup( //
			@Parameter(value = LOOKUPID) final String lookupId, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final LookupTranslation translation = new LookupTranslation();
		translation.setName(lookupId);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().create(translation);

	}

	@JSONExported
	@Admin
	public void createForGisIcon( //
			@Parameter(value = ICONNAME) final String iconName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final GisIconTranslation translation = new GisIconTranslation();
		translation.setName(iconName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().create(translation);

	}

	@JSONExported
	@Admin
	public JsonResponse readForClass( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = FIELD) final String field //
	) {
		final ClassTranslation translation = ClassTranslation.newInstance() //
				.withName(className) //
				.withField(field) //
				.build();
		final Map<String, String> translations = translationLogic().read(translation);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForClassAttribute( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = ATTRIBUTENAME) final String attributeName, //
			@Parameter(value = FIELD) final String field //
	) {
		final AttributeClassTranslation translation = AttributeClassTranslation.newInstance() //
				.forClass(className) //
				.withName(attributeName) //
				.withField(field) //
				.build();
		final Map<String, String> translations = translationLogic().read(translation);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public JsonResponse readForDomain( //
			@Parameter(value = DOMAIN_NAME) final String domainName, //
			@Parameter(value = FIELD) final String field //
	) {
		final DomainTranslation translation = DomainTranslation.newInstance() //
				.withName(domainName) //
				.withField(field) //
				.build();
		final Map<String, String> translations = translationLogic().read(translation);
		return JsonResponse.success(translations);

	}

	@JSONExported
	@Admin
	public JsonResponse readForDomainAttribute( //
			@Parameter(value = DOMAIN_NAME) final String domainName, //
			@Parameter(value = ATTRIBUTENAME) final String attributeName, //
			@Parameter(value = FIELD) final String field //
	) {
		final AttributeDomainTranslation translation = new AttributeDomainTranslation();
		translation.setName(domainName);
		translation.setAttributeName(attributeName);
		translation.setField(field);
		final Map<String, String> translations = translationLogic().read(translation);
		return JsonResponse.success(translations);

	}

	@JSONExported
	@Admin
	public JsonResponse readForFilterView( //
			@Parameter(value = VIEWNAME) final String viewName, //
			@Parameter(value = FIELD) final String field) {
		final FilterViewTranslation translation = new FilterViewTranslation();
		translation.setName(viewName);
		translation.setField(field);
		final Map<String, String> translations = translationLogic().read(translation);
		return JsonResponse.success(translations);

	}

	@JSONExported
	@Admin
	public JsonResponse readForSqlView( //
			@Parameter(value = VIEWNAME) final String viewName, //
			@Parameter(value = FIELD) final String field) {
		final SqlViewTranslation translation = new SqlViewTranslation();
		translation.setName(viewName);
		translation.setField(field);
		final Map<String, String> translations = translationLogic().read(translation);
		return JsonResponse.success(translations);

	}

	@JSONExported
	@Admin
	public JsonResponse readForFilter( //
			@Parameter(value = FILTERNAME) final String filterName, //
			@Parameter(value = FIELD) final String field) {
		final FilterTranslation translation = new FilterTranslation();
		translation.setName(filterName);
		translation.setField(field);
		final Map<String, String> translations = translationLogic().read(translation);
		return JsonResponse.success(translations);

	}

	@JSONExported
	@Admin
	public JsonResponse readForInstanceName() {
		final InstanceNameTranslation translation = new InstanceNameTranslation();
		final Map<String, String> translations = translationLogic().read(translation);
		return JsonResponse.success(translations);

	}

	@JSONExported
	@Admin
	public JsonResponse readForWidget( //
			@Parameter(value = WIDGET_ID) final String widgetId, //
			@Parameter(value = FIELD) final String field //
	) {
		final WidgetTranslation translation = new WidgetTranslation();
		translation.setName(widgetId);
		translation.setField(field);
		final Map<String, String> translations = translationLogic().read(translation);
		return JsonResponse.success(translations);

	}

	@JSONExported
	@Admin
	public JsonResponse readForDashboard( //
			@Parameter(value = DASHBOARDNAME) final String dashboardName, //
			@Parameter(value = FIELD) final String field //
	) {
		final DashboardTranslation translation = new DashboardTranslation();
		translation.setName(dashboardName);
		translation.setField(field);
		final Map<String, String> translations = translationLogic().read(translation);
		return JsonResponse.success(translations);

	}

	@JSONExported
	@Admin
	public JsonResponse readForChart( //
			@Parameter(value = CHARTNAME) final String chartName, //
			@Parameter(value = FIELD) final String field //
	) {
		final ChartTranslation translation = new ChartTranslation();
		translation.setName(chartName);
		translation.setField(field);
		final Map<String, String> translations = translationLogic().read(translation);
		return JsonResponse.success(translations);

	}

	@JSONExported
	@Admin
	public JsonResponse readForReport( //
			@Parameter(value = REPORTNAME) final String reportName, //
			@Parameter(value = FIELD) final String field //
	) {
		final ReportTranslation translation = new ReportTranslation();
		translation.setName(reportName);
		translation.setField(field);
		final Map<String, String> translations = translationLogic().read(translation);
		return JsonResponse.success(translations);

	}

	@JSONExported
	@Admin
	public JsonResponse readForLookup( //
			@Parameter(value = LOOKUPID) final String lookupId, //
			@Parameter(value = FIELD) final String field //
	) {
		final LookupTranslation translation = new LookupTranslation();
		translation.setName(lookupId);
		translation.setField(field);
		final Map<String, String> translations = translationLogic().read(translation);
		return JsonResponse.success(translations);

	}

	@JSONExported
	@Admin
	public JsonResponse readForGisIcon( //
			@Parameter(value = ICONNAME) final String iconName, //
			@Parameter(value = FIELD) final String field //
	) {
		final GisIconTranslation translation = new GisIconTranslation();
		translation.setName(iconName);
		translation.setField(field);
		final Map<String, String> translations = translationLogic().read(translation);
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
		final ClassTranslation translation = ClassTranslation.newInstance() //
				.withName(className) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().update(translation);
	}

	@JSONExported
	@Admin
	public void updateForClassAttribute( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = ATTRIBUTENAME) final String attributeName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final AttributeClassTranslation translation = AttributeClassTranslation.newInstance() //
				.forClass(className) //
				.withName(attributeName) //
				.withField(field) //
				.build();
		translationLogic().update(translation);
	}

	@JSONExported
	@Admin
	public void updateForDomain( //
			@Parameter(value = DOMAIN_NAME) final String domainName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final DomainTranslation translation = DomainTranslation.newInstance() //
				.withName(domainName) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().update(translation);

	}

	@JSONExported
	@Admin
	public void updateForDomainAttribute( //
			@Parameter(value = DOMAIN_NAME) final String domainName, //
			@Parameter(value = ATTRIBUTENAME) final String attributeName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final DomainTranslation translation = DomainTranslation.newInstance() //
				.withName(domainName) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().update(translation);

	}

	@JSONExported
	@Admin
	public void updateForFilterView( //
			@Parameter(value = VIEWNAME) final String viewName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final FilterViewTranslation translation = new FilterViewTranslation();
		translation.setName(viewName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().update(translation);

	}

	@JSONExported
	@Admin
	public void updateForSqlView( //
			@Parameter(value = VIEWNAME) final String viewName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final SqlViewTranslation translation = new SqlViewTranslation();
		translation.setName(viewName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().update(translation);

	}

	@JSONExported
	@Admin
	public void updateForFilter( //
			@Parameter(value = FILTERNAME) final String filterName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final FilterTranslation translation = new FilterTranslation();
		translation.setName(filterName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().update(translation);

	}

	@JSONExported
	@Admin
	public void updateForInstanceName( //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final InstanceNameTranslation translation = new InstanceNameTranslation();
		translation.setTranslations(toMap(translations));
		translationLogic().update(translation);

	}

	@JSONExported
	@Admin
	public void updateForWidget( //
			@Parameter(value = WIDGET_ID) final String widgetId, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final WidgetTranslation translation = new WidgetTranslation();
		translation.setName(widgetId);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().update(translation);

	}

	@JSONExported
	@Admin
	public void updateForDashboard( //
			@Parameter(value = DASHBOARDNAME) final String dashboardName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final DashboardTranslation translation = new DashboardTranslation();
		translation.setName(dashboardName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().update(translation);

	}

	@JSONExported
	@Admin
	public void updateForChart( //
			@Parameter(value = CHARTNAME) final String chartName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ChartTranslation translation = new ChartTranslation();
		translation.setName(chartName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().update(translation);

	}

	@JSONExported
	@Admin
	public void updateForReport( //
			@Parameter(value = REPORTNAME) final String reportName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ReportTranslation translation = new ReportTranslation();
		translation.setName(reportName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().update(translation);

	}

	@JSONExported
	@Admin
	public void updateForLookup( //
			@Parameter(value = LOOKUPID) final String lookupId, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final LookupTranslation translation = new LookupTranslation();
		translation.setName(lookupId);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().update(translation);

	}

	@JSONExported
	@Admin
	public void updateForGisIcon( //
			@Parameter(value = ICONNAME) final String iconName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final GisIconTranslation translation = new GisIconTranslation();
		translation.setName(iconName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().update(translation);

	}

	@JSONExported
	@Admin
	public void deleteForClass( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ClassTranslation translation = ClassTranslation.newInstance() //
				.withName(className) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().delete(translation);
	}

	@JSONExported
	@Admin
	public void deleteForClassAttribute( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = ATTRIBUTENAME) final String attributeName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final AttributeClassTranslation translation = AttributeClassTranslation.newInstance() //
				.forClass(className) //
				.withName(attributeName) //
				.withField(field) //
				.build();
		translationLogic().delete(translation);

	}

	@JSONExported
	@Admin
	public void deleteForDomain( //
			@Parameter(value = DOMAIN_NAME) final String domainName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final DomainTranslation translation = DomainTranslation.newInstance() //
				.withName(domainName) //
				.withField(field) //
				.withTranslations(toMap(translations)) //
				.build();
		translationLogic().delete(translation);
	}

	@JSONExported
	@Admin
	public void deleteForDomainAttribute( //
			@Parameter(value = DOMAIN_NAME) final String domainName, //
			@Parameter(value = ATTRIBUTENAME) final String attributeName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final AttributeDomainTranslation translation = new AttributeDomainTranslation();
		translation.setName(domainName);
		translation.setAttributeName(attributeName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().delete(translation);

	}

	@JSONExported
	@Admin
	public void deleteForFilterView( //
			@Parameter(value = VIEWNAME) final String viewName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final FilterViewTranslation translation = new FilterViewTranslation();
		translation.setName(viewName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().delete(translation);

	}

	@JSONExported
	@Admin
	public void deleteForSqlView( //
			@Parameter(value = VIEWNAME) final String viewName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final SqlViewTranslation translation = new SqlViewTranslation();
		translation.setName(viewName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().delete(translation);

	}

	@JSONExported
	@Admin
	public void deleteForFilter( //
			@Parameter(value = FILTERNAME) final String filterName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final FilterTranslation translation = new FilterTranslation();
		translation.setName(filterName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().delete(translation);

	}

	@JSONExported
	@Admin
	public void deleteForInstanceName( //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final InstanceNameTranslation translation = new InstanceNameTranslation();
		translation.setTranslations(toMap(translations));
		translationLogic().delete(translation);

	}

	@JSONExported
	@Admin
	public void deleteForWidget( //
			@Parameter(value = WIDGET_ID) final String widgetId, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final WidgetTranslation translation = new WidgetTranslation();
		translation.setName(widgetId);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().delete(translation);

	}

	@JSONExported
	@Admin
	public void deleteForDashboard( //
			@Parameter(value = DASHBOARDNAME) final String dashboardName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final DashboardTranslation translation = new DashboardTranslation();
		translation.setName(dashboardName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().delete(translation);

	}

	@JSONExported
	@Admin
	public void deleteForChart( //
			@Parameter(value = CHARTNAME) final String chartName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ChartTranslation translation = new ChartTranslation();
		translation.setName(chartName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().delete(translation);

	}

	@JSONExported
	@Admin
	public void deleteForReport( //
			@Parameter(value = REPORTNAME) final String reportName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final ReportTranslation translation = new ReportTranslation();
		translation.setName(reportName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().delete(translation);

	}

	@JSONExported
	@Admin
	public void deleteForLookup( //
			@Parameter(value = LOOKUPID) final String lookupId, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final LookupTranslation translation = new LookupTranslation();
		translation.setName(lookupId);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().delete(translation);

	}

	@JSONExported
	@Admin
	public void deleteForGisIcon( //
			@Parameter(value = ICONNAME) final String iconName, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final GisIconTranslation translation = new GisIconTranslation();
		translation.setName(iconName);
		translation.setField(field);
		translation.setTranslations(toMap(translations));
		translationLogic().delete(translation);

	}

}