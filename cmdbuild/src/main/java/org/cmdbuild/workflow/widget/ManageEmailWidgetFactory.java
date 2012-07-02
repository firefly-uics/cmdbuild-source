package org.cmdbuild.workflow.widget;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.cmdbuild.model.widget.ManageEmail;
import org.cmdbuild.model.widget.ManageEmail.Template;
import org.cmdbuild.model.widget.Widget;

public class ManageEmailWidgetFactory extends ValuePairWidgetFactory {

	private final static String TO_ADDRESSES = "ToAddresses";
	private final static String CC_ADDRESSES = "CCAddresses";
	private final static String SUBJECT = "Subject";
	private final static String CONTENT = "Content";
	private final static String CONDITION = "Condition";
	private final static String READ_ONLY = "ReadOnly";

	@Override
	public String getWidgetName() {
		return "manageEmail";
	}

	/*
	 * naive but fast to write solution
	 * ...first do it works...
	 */
	@Override
	protected Widget createWidget(Map<String, String> valueMap) {
		ManageEmail widget = new ManageEmail();
		Map<String, Template> templates = new LinkedHashMap<String, Template>(); // I want preserve the order

		Map<String, String> toAddresses = getAttributesStartingWith(valueMap, TO_ADDRESSES);
		for (String key: toAddresses.keySet()) {
			Template t = getTemplateForKey(key, templates, TO_ADDRESSES);
			t.setToAddresses(valueMap.get(key));
		}

		Map<String, String> ccAddresses = getAttributesStartingWith(valueMap, CC_ADDRESSES);
		for (String key: ccAddresses.keySet()) {
			Template t = getTemplateForKey(key, templates, CC_ADDRESSES);
			t.setCcAddresses(valueMap.get(key));
		}

		Map<String, String> subjects = getAttributesStartingWith(valueMap, SUBJECT);
		for (String key: subjects.keySet()) {
			Template t = getTemplateForKey(key, templates, SUBJECT);
			t.setSubject(valueMap.get(key));
		}

		Map<String, String> contents = getAttributesStartingWith(valueMap, CONTENT);
		for (String key: contents.keySet()) {
			Template t = getTemplateForKey(key, templates, CONTENT);
			t.setContent(valueMap.get(key));
		}

		Map<String, String> conditions = getAttributesStartingWith(valueMap, CONDITION);
		for (String key: conditions.keySet()) {
			Template t = getTemplateForKey(key, templates, CONDITION);
			t.setCondition(valueMap.get(key));
		}

		widget.setTemplates(templates.values());
		widget.setReadOnly(readBoolean(valueMap.get(READ_ONLY)));

		return widget;
	}

	private Map<String, String> getAttributesStartingWith(Map<String, String> valueMap, String prefix) {
		Map<String, String> out = new HashMap<String, String>();

		for (String key: valueMap.keySet()) {
			if (key.startsWith(prefix)) {
				out.put(key, valueMap.get(key));
			}
		}

		return out;
	}

	private Template getTemplateForKey(String key, Map<String, Template> templates, String attributeName) {
		String postFix = key.replaceFirst(attributeName, "");
		if ("".equals(postFix)) {
			postFix = "implicitTemplateName";
		}

		if (templates.containsKey(postFix)) {
			return templates.get(postFix);
		} else {
			Template t = new Template();
			templates.put(postFix, t);
			return t;
		}
	}
}
