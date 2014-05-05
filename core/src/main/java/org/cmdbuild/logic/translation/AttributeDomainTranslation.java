package org.cmdbuild.logic.translation;


public class AttributeDomainTranslation extends BaseTranslation {

	private	 String attributeName;

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	@Override
	public void accept(TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

}
