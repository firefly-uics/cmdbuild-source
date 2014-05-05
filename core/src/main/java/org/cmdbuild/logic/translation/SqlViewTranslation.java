package org.cmdbuild.logic.translation;


public class SqlViewTranslation extends BaseTranslation {

	@Override
	public void accept(TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

}