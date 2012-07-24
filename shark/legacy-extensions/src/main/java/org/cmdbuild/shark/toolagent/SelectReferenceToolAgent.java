package org.cmdbuild.shark.toolagent;

import static org.cmdbuild.common.Constants.CLASS_ID_ATTRIBUTE;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;

import java.util.List;

import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.workflow.type.ReferenceType;

public class SelectReferenceToolAgent extends AbstractConditionalToolAgent {

	private static final String CLASS_NAME = "ClassName";
	private static final String CODE = "Code";
	private static final String ATTRIBUTE_NAME = "AttributeName";
	private static final String ATTRIBUTE_VALUE = "AttributeValue";

	private static final String OUTPUT = "OutRef";

	@Override
	protected void innerInvoke() throws Exception {
		final String className = getParameterValue(CLASS_NAME);
		final String attributeName;
		final String attributeValue;
		if (hasParameter(CODE)) {
			attributeName = CODE_ATTRIBUTE;
			attributeValue = getParameterValue(CODE);
		} else {
			attributeName = getParameterValue(ATTRIBUTE_NAME);
			attributeValue = getParameterValue(ATTRIBUTE_VALUE);
		}

		final List<Card> cards = getFluentApi() //
				.queryClass(className) //
				.with(attributeName, attributeValue) //
				.fetch();
		final ReferenceType referenceType = cards.isEmpty() ? emptyReference() : referenceOf(firstOf(cards));

		setParameterValue(OUTPUT, referenceType);
	}

	private ReferenceType emptyReference() {
		return new ReferenceType();
	}

	private Card firstOf(final List<Card> cards) {
		return cards.get(0);
	}

	private ReferenceType referenceOf(final Card card) {
		final ReferenceType reference = emptyReference();
		reference.setId(card.getId());
		if (card.has(CLASS_ID_ATTRIBUTE)) {
			reference.setIdClass(card.getClassId());
		}
		if (card.has(DESCRIPTION_ATTRIBUTE)) {
			reference.setDescription(card.getDescription());
		}
		return reference;
	}

}
