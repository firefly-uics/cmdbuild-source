package org.cmdbuild.shark.toolagent;

import org.cmdbuild.workflow.type.ReferenceType;

public class ManageRelationToolAgent extends AbstractConditionalToolAgent {

	private static final String DOMAIN_NAME = "DomainName";
	private static final String CLASS_NAME_PREFIX = "ClassName";
	private static final String OBJ_ID_PREFIX = "ObjId";
	private static final String OBJ_REFERENCE_PREFIX = "ObjReference";
	private static final String DONE = "Done";

	private static final boolean RESULT_ALWAYS_TRUE_OR_THROWS = true;

	@Override
	protected void innerInvoke() throws Exception {
		final String domainName = getParameterValue(DOMAIN_NAME);

		final CardRef card1 = getCard1();
		final CardRef card2 = getCard2();

		getWorkflowApi().createRelation(domainName, //
				card1.className, card1.cardId, //
				card2.className, card2.cardId);

		setParameterValue(DONE, RESULT_ALWAYS_TRUE_OR_THROWS);
	}

	private CardRef getCard1() {
		return getCard(1);
	}

	private CardRef getCard2() {
		return getCard(2);
	}

	private CardRef getCard(int side) {
		final String className;
		final int cardId;
		if (hasParameter(CLASS_NAME_PREFIX + side)) {
			className = getParameterValue(CLASS_NAME_PREFIX + side);
			final Long objId = getParameterValue(OBJ_ID_PREFIX + side);
			cardId = objId.intValue();
		} else {
			final ReferenceType objReference = getParameterValue(OBJ_REFERENCE_PREFIX + side);
			className = getSchemaApi().findClass(objReference.getIdClass()).getName();
			cardId = objReference.getId();
		}
		return new CardRef(className, cardId);
	}

}
