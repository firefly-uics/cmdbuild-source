package org.cmdbuild.api.fluent;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import java.util.HashSet;
import java.util.Set;

public class ExistingCard extends ActiveCard {

	private final Set<String> requestedAttributes;
	private final Set<String> unmodifiableRequestedAttributes;

	ExistingCard(final FluentApi api, final String className, final Integer id) {
		super(api, className, id);
		requestedAttributes = new HashSet<String>();
		unmodifiableRequestedAttributes = unmodifiableSet(requestedAttributes);
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
		return withAttribute(name, value);
	}

	public ExistingCard withAttribute(final String name, final Object value) {
		super.set(name, value);
		return this;
	}

	public ExistingCard limitAttributes(final String... names) {
		requestedAttributes.addAll(asList(names));
		return this;
	}

	public Set<String> getRequestedAttributes() {
		return unmodifiableRequestedAttributes;
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
