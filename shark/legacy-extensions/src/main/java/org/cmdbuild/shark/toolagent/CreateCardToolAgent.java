package org.cmdbuild.shark.toolagent;

import static java.util.Arrays.*;
import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.workflow.type.ReferenceType;

public class CreateCardToolAgent extends AbstractConditionalToolAgent {

	private static final String CREATE_CARD = "createCard";
	private static final String CREATE_CARD_REF = "createCardRef";

	private static final String CLASS_NAME = "ClassName";
	private static final String CARD_CODE = "CardCode";
	private static final String CARD_DESCRIPTION = "CardDescription";
	private static final String CARD_ID = "CardId";
	private static final String CARD_REFERENCE = "CardReference";

	@Override
	protected void innerInvoke() throws Exception {
		if (asList(CREATE_CARD, CREATE_CARD_REF).contains(getId())) {
			final String classname = getParameterValue(CLASS_NAME);

			final Map<String, String> attributes = new HashMap<String, String>();
			final String code = getParameterValue(CARD_CODE);
			attributes.put("Code", code);
			final String description = getParameterValue(CARD_DESCRIPTION);
			attributes.put("Description", description);

			final int id = getWorkflowApi().createCard(classname, attributes);

			final String outParamName;
			final Object outParamValue;
			if (CREATE_CARD.equals(getId())) {
				outParamName = CARD_ID;
				outParamValue = id;
			} else {
				final ReferenceType reference = new ReferenceType();
				reference.setId(id);
				// TODO idClass???
				reference.setDescription(description);

				outParamName = CARD_REFERENCE;
				outParamValue = reference;
			}
			setParameterValue(outParamName, outParamValue);
		} else {
			final String message = format("invalid tool id '%s' for class '%s'", getId(), this.getClass().getName());
			throw new IllegalArgumentException(message);
		}
	}

}
