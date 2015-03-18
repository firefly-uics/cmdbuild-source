package org.cmdbuild.logic.translation;

import org.cmdbuild.logic.translation.object.ClassAttributeDescription;
import org.cmdbuild.logic.translation.object.ClassAttributeGroup;
import org.cmdbuild.logic.translation.object.ClassDescription;
import org.cmdbuild.logic.translation.object.DomainAttributeDescription;
import org.cmdbuild.logic.translation.object.DomainDescription;
import org.cmdbuild.logic.translation.object.DomainDirectDescription;
import org.cmdbuild.logic.translation.object.DomainInverseDescription;
import org.cmdbuild.logic.translation.object.DomainMasterDetailLabel;
import org.cmdbuild.logic.translation.object.LookupDescription;
import org.cmdbuild.logic.translation.object.MenuItemDescription;

public interface TranslationObjectVisitor {

	void visit(ClassAttributeDescription classAttributeDescription);

	void visit(ClassAttributeGroup classAttributeGroup);

	void visit(ClassDescription classDescription);

	void visit(DomainAttributeDescription domainAttributeDescription);

	void visit(DomainDescription domainDescription);

	void visit(DomainDirectDescription domainDirectDescription);

	void visit(DomainInverseDescription domainInverseDescription);

	void visit(DomainMasterDetailLabel domainMasterDetailDescription);

	void visit(FilterTranslation filterTranslation);

	void visit(InstanceNameTranslation instanceNameTranslation);

	void visit(LookupDescription lookupDescription);

	void visit(MenuItemDescription menuItemDescription);

	void visit(NullTranslationObject translationObject);

	void visit(ReportTranslation reportTranslation);

	void visit(ViewTranslation filterViewTranslation);

	void visit(WidgetTranslation widgetTranslation);

}
