package org.cmdbuild.logic.translation;


public class FilterViewTranslation extends BaseTranslation {

	@Override
	public void accept(TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

}