package org.cmdbuild.api.fluent;

public class ExistingCard extends ActiveCard {

	public ExistingCard(final FluentApiExecutor apiExecutor) {
		super(apiExecutor);
	}

	public ExistingCard forClassName(final String className) {
		super.setClassName(className);
		return this;
	}

	public ExistingCard withId(final int id) {
		super.setId(id);
		return this;
	}

	public ExistingCard withCode(final String value) {
		super.addCodeAttribute(value);
		return this;
	}

	public ExistingCard withDescription(final String value) {
		super.addDescriptionAttribute(value);
		return this;
	}

	public ExistingCard with(final String name, final String value) {
		super.addAttribute(name, value);
		return this;
	}

	public ExistingCard withAttribute(final String name, final String value) {
		super.addAttribute(name, value);
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
