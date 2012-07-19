package org.cmdbuild.api.fluent;

import static java.util.Collections.unmodifiableMap;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;

import java.util.HashMap;
import java.util.Map;

public class Card extends CardDescriptor {

	private final Map<String, String> attributes = new HashMap<String, String>();

	public Map<String, String> getAttributes() {
		return unmodifiableMap(attributes);
	}

	public String getCode() {
		return attributes.get(CODE_ATTRIBUTE);
	}

	public String getDescription() {
		return attributes.get(DESCRIPTION_ATTRIBUTE);
	}

	void addAttribute(final String name, final String value) {
		attributes.put(name, value);
	}

	void addCodeAttribute(final String value) {
		addAttribute(CODE_ATTRIBUTE, value);
	}

	void addDescriptionAttribute(final String value) {
		addAttribute(DESCRIPTION_ATTRIBUTE, value);
	}

}