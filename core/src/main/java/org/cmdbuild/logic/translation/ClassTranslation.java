package org.cmdbuild.logic.translation;


public class ClassTranslation extends BaseTranslation {

	@Override
	public void accept(TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

}
