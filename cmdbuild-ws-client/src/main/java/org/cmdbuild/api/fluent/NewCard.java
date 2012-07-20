package org.cmdbuild.api.fluent;

public class NewCard extends ActiveCard {

	public NewCard(final String className, final FluentApiExecutor executor) {
		super(className, null, executor);
	}

	public NewCard withCode(final String value) {
		super.setCode(value);
		return this;
	}

	public NewCard withDescription(final String value) {
		super.setDescription(value);
		return this;
	}

	public NewCard with(final String name, final String value) {
		super.set(name, value);
		return this;
	}

	public NewCard withAttribute(final String name, final String value) {
		super.set(name, value);
		return this;
	}

	public CardDescriptor create() {
		return executor().create(this);
	}

}