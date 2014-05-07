package org.cmdbuild.logic.translation;

public class WidgetTranslation extends BaseTranslation {

	@Override
	public void accept(final TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

}