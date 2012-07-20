package org.cmdbuild.shark.toolagent;

import org.cmdbuild.workflow.type.ReferenceType;


public class SelectAttributeToolAgent extends AbstractConditionalToolAgent {

	private static final String CLASS_NAME = "ClassName";
	private static final String OBJ_ID = "ObjId";
	private static final String OBJ_REFERENCE = "ObjReference";
	private static final String ATTRIBUTE_NAME = "AttributeName";

	private static final String ATTRIBUTE_VALUE = "AttributeValue";

	@Override
	protected void innerInvoke() throws Exception {
		final CardRef card = getCard();
		final String attributeName = getParameterValue(ATTRIBUTE_NAME);

		final String attributeValue = getWorkflowApi().selectAttribute(card.className, card.cardId, attributeName);

		setParameterValue(ATTRIBUTE_VALUE, attributeValue);
	}

	private CardRef getCard() {
		final String className;
		final int cardId;
		if (hasParameter(CLASS_NAME)) {
			className = getParameterValue(CLASS_NAME);
			final Long objId = getParameterValue(OBJ_ID);
			cardId = objId.intValue();
		} else {
			final ReferenceType objReference = getParameterValue(OBJ_REFERENCE);
			className = getSchemaApi().findClass(objReference.getIdClass()).getName();
			cardId = objReference.getId();
		}
		return new CardRef(className, cardId);
	}
}
