package org.cmdbuild.api.fluent;

import java.util.List;

public class QueryClass extends ActiveCard {

	QueryClass(final FluentApi api, final String className) {
		super(api, className, null);
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

	public List<Card> fetch() {
		return api().getExecutor().fetchCards(this);
	}

}
