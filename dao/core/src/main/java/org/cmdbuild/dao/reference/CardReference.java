package org.cmdbuild.dao.reference;

import org.cmdbuild.common.Constants;
import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.dao.entry.CMCard;

public class CardReference extends AbstractReference {

	private final String className;

	@Legacy("We should think about how to solve this problem after the 2.0 is out...")
	private final String description;

	public static CardReference newInstance(final String className, final Long cardId, final String description) {
		return new CardReference(className, cardId, description);
	}

	public static CardReference newInstance(final CMCard card) {
		if (card == null) {
			return null;
		}
		final String className = card.getType().getName();
		final Long cardId = card.getId();
		final String description = (String) card.get(Constants.DESCRIPTION_ATTRIBUTE);
		return newInstance(className, cardId, description);
	}

	private CardReference(final String className, final Long cardId, final String description) {
		super(cardId);
		this.className = className;
		this.description = description;
	}

	public String getClassName() {
		return className;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public void accept(CMReferenceVisitor visitor) {
		visitor.visit(this);
	}
}
