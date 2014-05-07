package org.cmdbuild.logic.translation;

public class ChartTranslation extends BaseTranslation {

	@Override
	public void accept(final TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

}