package org.cmdbuild.shark.toolagent;

import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.workflow.type.ReferenceType;

abstract class ManageAttributeToolAgent extends AbstractConditionalToolAgent {

	protected static final String CLASS_NAME = "ClassName";
	protected static final String OBJ_ID = "ObjId";
	protected static final String OBJ_REFERENCE = "ObjReference";

	protected static final String ATTRIBUTE_NAME = "AttributeName";
	private static final Object UNUSED_ATTRIBUTE_VALUE = null;

	@Override
	protected void innerInvoke() throws Exception {
		setParameterValue(outputName(), outputValue());
	}

	protected abstract String outputName();

	protected abstract Object outputValue();

	protected String attributeValue() {
		final String attributeName = getParameterValue(ATTRIBUTE_NAME);
		final Card card = existingCard()//
				.withAttribute(attributeName, UNUSED_ATTRIBUTE_VALUE) //
				.fetch();
		final String attributeValue = card.get(attributeName, String.class);
		return attributeValue;
	}

	private ExistingCard existingCard() {
		final String className;
		final int cardId;
		if (hasParameter(CLASS_NAME)) {
			className = getParameterValue(CLASS_NAME);
			final Long objId = getParameterValue(OBJ_ID);
			cardId = objId.intValue();
		} else {
			final ReferenceType objReference = getParameterValue(OBJ_REFERENCE);
			className = getWorkflowApi().findClass(objReference.getIdClass()).getName();
			cardId = objReference.getId();
		}
		return getWorkflowApi().existingCard(className, cardId);
	}

}
