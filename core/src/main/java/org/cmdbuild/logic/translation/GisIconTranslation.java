package org.cmdbuild.logic.translation;


public class GisIconTranslation extends BaseTranslation {

	@Override
	public void accept(TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

}