package org.cmdbuild.shark.toolagent;

import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.shark.Logging;
import org.cmdbuild.workflow.type.ReferenceType;

abstract class ManageAttributeToolAgent extends AbstractConditionalToolAgent {

	protected static final String CLASS_NAME = "ClassName";
	protected static final String OBJ_ID = "ObjId";
	protected static final String OBJ_REFERENCE = "ObjReference";

	protected static final String ATTRIBUTE_NAME = "AttributeName";

	@Override
	protected void innerInvoke() throws Exception {
		setParameterValue(outputName(), attributeValue());
	}

	protected abstract String outputName();

	protected Object attributeValue() {
		final String attributeName = getParameterValue(ATTRIBUTE_NAME);
		final Card card = existingCard()//
				.limitAttributes(attributeName) //
				.fetch();
		return card.get(attributeName);
	}

	private ExistingCard existingCard() {
		String className;
		final int cardId;
		if (hasParameter(CLASS_NAME)) {
			className = getParameterValue(CLASS_NAME);
			final Long objId = getParameterValue(OBJ_ID);
			cardId = objId.intValue();
		} else {
//			final ReferenceType objReference = getParameterValue(OBJ_REFERENCE);
//			className = getWorkflowApi().findClass(objReference.getIdClass()).getName();
//			cardId = objReference.getId();
			
			final ReferenceType objReference = getParameterValue(OBJ_REFERENCE);
			cardId = objReference.getId();
			int classId = objReference.getIdClass();
			try{
				className = getWorkflowApi().findClass(classId).getName();
			}
			//ugly hack to prevent idclass not found after a db restore
			catch(Exception e){
				cus.debug(shandle, Logging.LOGGER_NAME, e.getMessage());
				cus.debug(shandle, Logging.LOGGER_NAME, "To prevent this error we will use as a classname 'Class' because the classid "+classId+" was not found.");
				className = "Class";
			}
		}
		return getWorkflowApi().existingCard(className, cardId);
	}

}
