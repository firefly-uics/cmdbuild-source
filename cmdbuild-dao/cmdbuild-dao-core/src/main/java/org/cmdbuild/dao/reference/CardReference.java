package org.cmdbuild.dao.reference;


public class CardReference extends AbstractReference {

	private final String className;

	public static CardReference newInstance(final String className, final Object cardId) {
		return new CardReference(className, cardId);
	}

	private CardReference(final String className, final Object cardId) {
		super(cardId);
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public void accept(CMReferenceVisitor visitor) {
		visitor.visit(this);
	}
}
