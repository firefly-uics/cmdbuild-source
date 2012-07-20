package org.cmdbuild.api.fluent;

import java.util.List;

public class QueryClass extends ActiveCard {

	public QueryClass(final FluentApiExecutor executor, final String className) {
		super(executor, className, null);
	}

	public QueryClass withCode(final String value) {
		super.setCode(value);
		return this;
	}

	public QueryClass withDescription(final String value) {
		super.setDescription(value);
		return this;
	}

	public QueryClass with(final String name, final String value) {
		super.set(name, value);
		return this;
	}

	public QueryClass withAttribute(final String name, final String value) {
		super.set(name, value);
		return this;
	}

	public List<CardDescriptor> fetch() {
		return executor().fetch(this);
	}

}
