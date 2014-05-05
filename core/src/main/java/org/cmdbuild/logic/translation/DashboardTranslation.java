package org.cmdbuild.logic.translation;


public class DashboardTranslation extends BaseTranslation {

	@Override
	public void accept(TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

}