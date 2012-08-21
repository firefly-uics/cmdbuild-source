package org.cmdbuild.shark.toolagent;

import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.NewCard;
import org.cmdbuild.common.Constants;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.api.internal.toolagent.AppParameter;

public class CreateCardToolAgent extends ManageCardToolAgent {

	private static final String CREATE_CARD = "createCard";
	private static final String CREATE_CARD_REF = "createCardRef";

	private static final List<String> NOT_META_TOOLS = asList(CREATE_CARD, CREATE_CARD_REF);
	private static final List<String> NOT_META_ATTRIBUTES = asList(CLASS_NAME);

	private static final String CARD_CODE = "CardCode";
	private static final String CARD_DESCRIPTION = "CardDescription";

	@Override
	protected void innerInvoke() throws Exception {
		final String classname = getClassName();
		final Map<String, Object> attributes = getAttributeMap();
		final int newCardId = createCard(classname, attributes);

		final String description = String.valueOf(attributes.get(Constants.DESCRIPTION_ATTRIBUTE));
		for (final AppParameter parmOut : getReturnParameters()) {
			if (parmOut.the_class == Long.class) {
				parmOut.the_value = newCardId;
			} else if (parmOut.the_class == ReferenceType.class) {
				final ReferenceType reference = new ReferenceType();
				reference.setId(newCardId);
				reference.setIdClass(getWorkflowApi().findClass(classname).getId());
				reference.setDescription(description);
				parmOut.the_value = reference;
			}
		}
	}

	private int createCard(final String classname, final Map<String, Object> attributes) {
		final NewCard newCard = getWorkflowApi().newCard(classname);
		for (final Entry<String, Object> attribute : attributes.entrySet()) {
			newCard.with(attribute.getKey(), attribute.getValue());
		}
		final CardDescriptor cardDescriptor = newCard.create();
		final int newCardId = cardDescriptor.getId();
		return newCardId;
	}

	@Override
	protected List<String> notMetaToolNames() {
		return NOT_META_TOOLS;
	}

	@Override
	protected List<String> notMetaAttributeNames() {
		return NOT_META_ATTRIBUTES;
	}

	@Override
	protected Map<String, Object> getNonMetaAttributes() {
		final Map<String, Object> attributes = new HashMap<String, Object>();
		final String code = getParameterValue(CARD_CODE);
		attributes.put(Constants.CODE_ATTRIBUTE, code);
		final String description = getParameterValue(CARD_DESCRIPTION);
		attributes.put(Constants.DESCRIPTION_ATTRIBUTE, description);
		return attributes;
	}

}
