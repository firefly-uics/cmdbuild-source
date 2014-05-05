package org.cmdbuild.logic.translation;


public class InstanceNameTranslation extends BaseTranslation {

	@Override
	public void accept(TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

}