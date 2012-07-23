package org.cmdbuild.shark.toolagent;

import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.NewCard;
import org.cmdbuild.common.Constants;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.api.internal.toolagent.AppParameter;

public class CreateCardToolAgent extends AbstractConditionalToolAgent {

	private static final String CREATE_CARD = "createCard";
	private static final String CREATE_CARD_REF = "createCardRef";

	private static final String CLASS_NAME = "ClassName";
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
				final ReferenceType ref = new ReferenceType();
				ref.setId(newCardId);
				ref.setIdClass(getSchemaApi().findClass(classname).getId());
				ref.setDescription(description);
				parmOut.the_value = ref;
			}
		}
	}

	private int createCard(final String classname, final Map<String, Object> attributes) {
		final NewCard newCard = getFluentApi().newCard(classname);
		for (final Entry<String, Object> attribute : attributes.entrySet()) {
			newCard.with(attribute.getKey(), attribute.getValue().toString());
		}
		final CardDescriptor cardDescriptor = newCard.create();
		final int newCardId = cardDescriptor.getId();
		return newCardId;
	}

	private String getClassName() {
		String className = getExtendedAttribute(CLASS_NAME);
		if (className == null) {
			className = getParameterValue(CLASS_NAME);
		}
		return className;
	}

	private Map<String, Object> getAttributeMap() {
		final Map<String, Object> attributes = new HashMap<String, Object>();
		if (asList(CREATE_CARD, CREATE_CARD_REF).contains(getId())) {
			final String code = getParameterValue(CARD_CODE);
			attributes.put(Constants.CODE_ATTRIBUTE, code);
			final String description = getParameterValue(CARD_DESCRIPTION);
			attributes.put(Constants.DESCRIPTION_ATTRIBUTE, description);
		} else {
			for (final Map.Entry<String, Object> entry : getInputParameterValues().entrySet()) {
				if (CLASS_NAME.equals(entry.getKey()))
					continue;
				attributes.put(entry.getKey(), entry.getValue());
			}
		}
		return attributes;
	}

}
