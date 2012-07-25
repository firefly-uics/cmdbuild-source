package org.cmdbuild.shark.toolagent;

import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.workflow.type.ReferenceType;

public class UpdateAttributeToolAgent extends AbstractConditionalToolAgent {

	private static final String CLASS_NAME = "ClassName";
	private static final String OBJ_ID = "ObjId";
	private static final String OBJ_REFERENCE = "ObjReference";
	private static final String ATTRIBUTE_NAME = "AttributeName";
	private static final String ATTRIBUTE_VALUE = "AttributeValue";
	private static final String DONE = "Done";

	@Override
	protected void innerInvoke() throws Exception {
		final String attributeName = getParameterValue(ATTRIBUTE_NAME);
		final String attributeValue = getParameterValue(ATTRIBUTE_VALUE);
		existingCard()//
				.withAttribute(attributeName, attributeValue) //
				.update();
		setParameterValue(DONE, true);
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
