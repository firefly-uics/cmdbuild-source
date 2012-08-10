package org.cmdbuild.shark.toolagent;

import static org.apache.commons.lang.StringUtils.EMPTY;

import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.workflow.type.ReferenceType;

public class SelectReferenceByReferenceToolAgent extends ManageAttributeToolAgent {

	private static final String CLASS = "Class";

	private static final String OUT_REF = "OutRef";

	@Override
	protected String outputName() {
		return OUT_REF;
	}

	@Override
	protected Object outputValue() {
		final int referencedId = referencedId();
		final Card referencedCard = cardFor(referencedId);
		return referenceTypeFor(referencedCard);
	}

	private int referencedId() {
		final int referencedId = Integer.parseInt(attributeValue());
		return referencedId;
	}

	private Card cardFor(final int id) {
		return getFluentApi().existingCard(CLASS, id) //
				.fetch();
	}

	private ReferenceType referenceTypeFor(final Card card) {
		final String className = card.getClassName();
		final int idClass = getSchemaApi().findClass(className).getId();
		return new ReferenceType(card.getId(), idClass, EMPTY);
	}
}
