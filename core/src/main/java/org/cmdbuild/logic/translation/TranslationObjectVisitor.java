package org.cmdbuild.logic.translation;

public interface TranslationObjectVisitor {

	void visit(ClassTranslation translationObject);

	void visit(DomainTranslation domainTranslation);

	void visit(AttributeClassTranslation attributeClassTranslation);

	void visit(AttributeDomainTranslation attributeClassTranslation);

	void visit(FilterViewTranslation filterViewTranslation);

	void visit(SqlViewTranslation sqlViewTranslation);

	void visit(FilterTranslation filterTranslation);

	void visit(InstanceNameTranslation instanceNameTranslation);

	void visit(WidgetTranslation widgetTranslation);

	void visit(DashboardTranslation dashboardTranslation);

	void visit(ChartTranslation chartTranslation);

	void visit(ReportTranslation reportTranslation);

	void visit(LookupTranslation lookupTranslation);

	void visit(GisIconTranslation gisIconTranslation);

}
