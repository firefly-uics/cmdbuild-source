package org.cmdbuild.logic.translation;

import org.cmdbuild.logic.translation.object.ClassAttributeDescription;
import org.cmdbuild.logic.translation.object.ClassAttributeGroup;
import org.cmdbuild.logic.translation.object.ClassDescription;
import org.cmdbuild.logic.translation.object.DomainDescription;
import org.cmdbuild.logic.translation.object.DomainDirectDescription;
import org.cmdbuild.logic.translation.object.DomainInverseDescription;
import org.cmdbuild.logic.translation.object.DomainMasterDetailLabel;

public interface TranslationObjectVisitor {

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

	void visit(DomainDescription domainDescription);

	void visit(DomainDirectDescription domainDirectDescription);

	void visit(DomainInverseDescription domainInverseDescription);

	void visit(DomainMasterDetailLabel domainMasterDetailDescription);


}
