package org.cmdbuild.api.fluent;

public class ExistingCard extends AbstractCard {

	public ExistingCard(final FluentApiExecutor apiExecutor) {
		super(apiExecutor);
	}

	@Override
	public ExistingCard forClass(final String className) {
		super.forClass(className);
		return this;
	}

	@Override
	public ExistingCard withId(final int id) {
		super.withId(id);
		return this;
	}

	@Override
	public ExistingCard withCode(final String value) {
		super.withCode(value);
		return this;
	}

	@Override
	public ExistingCard withDescription(final String value) {
		super.withDescription(value);
		return this;
	}

	@Override
	public ExistingCard with(final String name, final String value) {
		super.with(name, value);
		return this;
	}

	@Override
	public ExistingCard withAttribute(final String name, final String value) {
		super.withAttribute(name, value);
		return this;
	}

	public void update() {
		executor().update(this);
	}

	public void delete() {
		executor().delete(this);
	}

}
