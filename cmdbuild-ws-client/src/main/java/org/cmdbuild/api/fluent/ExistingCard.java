package org.cmdbuild.api.fluent;

public class ExistingCard extends ActiveCard {

	ExistingCard(final FluentApi api, final String className, final Integer id) {
		super(api, className, id);
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
		api().getExecutor().update(this);
	}

	public void delete() {
		api().getExecutor().delete(this);
	}

	public Card fetch() {
		return api().getExecutor().fetch(this);
	}

}
