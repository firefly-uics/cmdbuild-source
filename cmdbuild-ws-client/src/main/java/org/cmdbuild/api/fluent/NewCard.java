package org.cmdbuild.api.fluent;

public class NewCard extends AbstractCard {

	public NewCard(final FluentApiExecutor executor) {
		super(executor);
	}

	@Override
	public NewCard forClass(final String className) {
		super.forClass(className);
		return this;
	}

	@Override
	public NewCard withCode(final String value) {
		super.withCode(value);
		return this;
	}

	@Override
	public NewCard withDescription(final String value) {
		super.withDescription(value);
		return this;
	}

	@Override
	public NewCard with(final String name, final String value) {
		super.with(name, value);
		return this;
	}

	@Override
	public NewCard withAttribute(final String name, final String value) {
		super.withAttribute(name, value);
		return this;
	}

	public CardDescriptor create() {
		return executor().create(this);
	}

}