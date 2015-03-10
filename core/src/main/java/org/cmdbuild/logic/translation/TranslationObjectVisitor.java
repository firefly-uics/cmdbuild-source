package org.cmdbuild.logic.translation;

public interface TranslationObjectVisitor {

	void visit(DomainTranslation domainTranslation);

	void visit(AttributeDomainTranslation attributeClassTranslation);

	void visit(ViewTranslation filterViewTranslation);

	void visit(FilterTranslation filterTranslation);

	void visit(InstanceNameTranslation instanceNameTranslation);

	void visit(WidgetTranslation widgetTranslation);

	void visit(DashboardTranslation dashboardTranslation);

	void visit(ChartTranslation chartTranslation);

	void visit(ReportTranslation reportTranslation);

	void visit(LookupTranslation lookupTranslation);

	void visit(GisIconTranslation gisIconTranslation);

	void visit(MenuItemTranslation menuItemTranslation);

	void visit(NullTranslationObject translationObject);

	void visit(ClassDescription classDescription);

	void visit(ClassAttributeDescription classAttributeDescription);

	void visit(ClassAttributeGroup classAttributeGroup);

}
