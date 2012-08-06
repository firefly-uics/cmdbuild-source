package org.cmdbuild.workflow.widget;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.logic.EmailLogic;
import org.cmdbuild.model.widget.ManageEmail;
import org.cmdbuild.model.widget.ManageEmail.EmailTemplate;
import org.cmdbuild.model.widget.Widget;

public class ManageEmailWidgetFactory extends ValuePairWidgetFactory {

	private final static String TO_ADDRESSES = "ToAddresses";
	private final static String CC_ADDRESSES = "CCAddresses";
	private final static String SUBJECT = "Subject";
	private final static String CONTENT = "Content";
	private final static String CONDITION = "Condition";
	private final static String READ_ONLY = "ReadOnly";
	private final static String LABEL = "ButtonLabel";

	private final static String WIDGET_NAME = "manageEmail";

	private final EmailLogic emailLogic;

	public ManageEmailWidgetFactory(final EmailLogic emailLogic) {
		this.emailLogic = emailLogic;
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	/*
	 * naive but fast to write solution
	 * ...first do it works...
	 */
	@Override
	protected Widget createWidget(Map<String, Object> valueMap) {
		ManageEmail widget = new ManageEmail(emailLogic);
		Map<String, EmailTemplate> emailTemplate = new LinkedHashMap<String, EmailTemplate>(); // I want preserve the order
		Set<String> managedParameters = new HashSet<String>();
		managedParameters.add(READ_ONLY);
		managedParameters.add(LABEL);

		Map<String, String> toAddresses = getAttributesStartingWith(valueMap, TO_ADDRESSES);
		for (String key: toAddresses.keySet()) {
			EmailTemplate t = getTemplateForKey(key, emailTemplate, TO_ADDRESSES);
			t.setToAddresses(readString(valueMap.get(key)));
		}
		managedParameters.addAll(toAddresses.keySet());

		Map<String, String> ccAddresses = getAttributesStartingWith(valueMap, CC_ADDRESSES);
		for (String key: ccAddresses.keySet()) {
			EmailTemplate t = getTemplateForKey(key, emailTemplate, CC_ADDRESSES);
			t.setCcAddresses(readString(valueMap.get(key)));
		}
		managedParameters.addAll(ccAddresses.keySet());

		Map<String, String> subjects = getAttributesStartingWith(valueMap, SUBJECT);
		for (String key: subjects.keySet()) {
			EmailTemplate t = getTemplateForKey(key, emailTemplate, SUBJECT);
			t.setSubject(readString(valueMap.get(key)));
		}
		managedParameters.addAll(subjects.keySet());

		Map<String, String> contents = getAttributesStartingWith(valueMap, CONTENT);
		for (String key: contents.keySet()) {
			EmailTemplate t = getTemplateForKey(key, emailTemplate, CONTENT);
			t.setContent(readString(valueMap.get(key)));
		}
		managedParameters.addAll(contents.keySet());

		Map<String, String> conditions = getAttributesStartingWith(valueMap, CONDITION);
		for (String key: conditions.keySet()) {
			EmailTemplate t = getTemplateForKey(key, emailTemplate, CONDITION);
			t.setCondition(readString(valueMap.get(key)));
		}
		managedParameters.addAll(conditions.keySet());

		widget.setEmailTemplates(emailTemplate.values());
		widget.setTemplates(extractUnmanagedStringParameters(valueMap, managedParameters));
		widget.setReadOnly(readBooleanTrueIfPresent(valueMap.get(READ_ONLY)));

		return widget;
	}

	private Map<String, String> getAttributesStartingWith(Map<String, Object> valueMap, String prefix) {
		Map<String, String> out = new HashMap<String, String>();

		for (final String key: valueMap.keySet()) {
			if (key.startsWith(prefix)) {
				out.put(key, readString(valueMap.get(key)));
			}
		}

		return out;
	}

	private EmailTemplate getTemplateForKey(String key, Map<String, EmailTemplate> templates, String attributeName) {
		String postFix = key.replaceFirst(attributeName, "");
		if ("".equals(postFix)) {
			postFix = "implicitTemplateName";
		}

		if (templates.containsKey(postFix)) {
			return templates.get(postFix);
		} else {
			EmailTemplate t = new EmailTemplate();
			templates.put(postFix, t);
			return t;
		}
	}
}
