package org.cmdbuild.api.fluent;

import static java.util.Collections.unmodifiableMap;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCard {

	private final FluentApiExecutor executor;

	private String className;
	private int id;

	private final Map<String, String> attributes;

	public AbstractCard(final FluentApiExecutor executor) {
		this.executor = executor;
		this.attributes = new HashMap<String, String>();
	}

	protected FluentApiExecutor executor() {
		return executor;
	}

	public String getClassName() {
		return className;
	}

	public AbstractCard forClass(final String className) {
		this.className = className;
		return this;
	}

	public int getId() {
		return id;
	}

	public AbstractCard withId(final int id) {
		this.id = id;
		return this;
	}

	public Map<String, String> getAttributes() {
		return unmodifiableMap(attributes);
	}

	public AbstractCard withCode(final String value) {
		return withAttribute(CODE_ATTRIBUTE, value);
	}

	public AbstractCard withDescription(final String value) {
		return withAttribute(DESCRIPTION_ATTRIBUTE, value);
	}

	public AbstractCard with(final String name, final String value) {
		return withAttribute(name, value);
	}

	public AbstractCard withAttribute(final String name, final String value) {
		attributes.put(name, value);
		return this;
	}

}
