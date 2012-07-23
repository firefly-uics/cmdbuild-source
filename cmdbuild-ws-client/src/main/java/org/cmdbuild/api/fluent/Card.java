package org.cmdbuild.api.fluent;

import static java.util.Collections.unmodifiableMap;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;

import java.util.HashMap;
import java.util.Map;

public class Card extends CardDescriptor {

	private final Map<String, Object> attributes;

	public Card(final String className, final Integer id) {
		super(className, id);
		attributes = new HashMap<String, Object>();
	}

	public Map<String, Object> getAttributes() {
		return unmodifiableMap(attributes);
	}

	public String getCode() {
		return get(CODE_ATTRIBUTE, String.class);
	}

	public String getDescription() {
		return get(DESCRIPTION_ATTRIBUTE, String.class);
	}

	public Object get(final String name) {
		return attributes.get(name);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(final String name, final Class<T> clazz) {
		return (T) attributes.get(name);
	}

	void set(final String name, final Object value) {
		attributes.put(name, value);
	}

	void setCode(final String value) {
		set(CODE_ATTRIBUTE, value);
	}

	void setDescription(final String value) {
		set(DESCRIPTION_ATTRIBUTE, value);
	}

}