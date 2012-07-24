package org.cmdbuild.shark.toolagent;

import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.workflow.type.ReferenceType;

public class SelectAttributeToolAgent extends AbstractConditionalToolAgent {

	private static final String CLASS_NAME = "ClassName";
	private static final String OBJ_ID = "ObjId";
	private static final String OBJ_REFERENCE = "ObjReference";
	private static final String ATTRIBUTE_NAME = "AttributeName";

	private static final String ATTRIBUTE_VALUE = "AttributeValue";

	@Override
	protected void innerInvoke() throws Exception {
		final String attributeName = getParameterValue(ATTRIBUTE_NAME);
		final Card card = existingCard() //
				.withAttribute(attributeName, null) //
				.fetch();
		final String attributeValue = card.get(attributeName, String.class);
		setParameterValue(ATTRIBUTE_VALUE, attributeValue);
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
			className = getSchemaApi().findClass(objReference.getIdClass()).getName();
			cardId = objReference.getId();
		}
		return getFluentApi().existingCard(className, cardId);
	}

}
