package org.cmdbuild.api.fluent;

public class NewCard extends ActiveCard {

	public NewCard(final FluentApiExecutor executor) {
		super(executor);
	}

	public NewCard forClassName(final String className) {
		super.setClassName(className);
		return this;
	}

	public NewCard withCode(final String value) {
		super.addCodeAttribute(value);
		return this;
	}

	public NewCard withDescription(final String value) {
		super.addDescriptionAttribute(value);
		return this;
	}

	public NewCard with(final String name, final String value) {
		super.addAttribute(name, value);
		return this;
	}

	public NewCard withAttribute(final String name, final String value) {
		super.addAttribute(name, value);
		return this;
	}

	public CardDescriptor create() {
		return executor().create(this);
	}

}