package org.cmdbuild.api.fluent;

public class ExistingCard extends ActiveCard {

	public ExistingCard(final FluentApiExecutor executor, final String className, final Integer id) {
		super(executor, className, id);
	}

	public ExistingCard withCode(final String value) {
		super.setCode(value);
		return this;
	}

	public ExistingCard withDescription(final String value) {
		super.setDescription(value);
		return this;
	}

	public ExistingCard with(final String name, final Object value) {
		super.set(name, value);
		return this;
	}

	public ExistingCard withAttribute(final String name, final Object value) {
		super.set(name, value);
		return this;
	}

	public void update() {
		executor().update(this);
	}

	public void delete() {
		executor().delete(this);
	}

	public Card fetch() {
		return executor().fetch(this);
	}

}
