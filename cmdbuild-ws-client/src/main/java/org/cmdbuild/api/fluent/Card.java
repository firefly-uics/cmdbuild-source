package org.cmdbuild.api.fluent;

import static java.util.Collections.unmodifiableMap;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;

import java.util.HashMap;
import java.util.Map;

public class Card {

	private String className;
	private int id;
	private final Map<String, String> attributes;

	public Card() {
		this.attributes = new HashMap<String, String>();
	}

	public String getClassName() {
		return className;
	}

	void setClassName(final String className) {
		this.className = className;
	}

	public int getId() {
		return id;
	}

	void setId(final int id) {
		this.id = id;
	}

	public Map<String, String> getAttributes() {
		return unmodifiableMap(attributes);
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