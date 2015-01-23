package org.cmdbuild.logic.data.access.filter.model;

import static com.google.common.collect.Sets.newHashSet;

public abstract class CompositeElement extends AbstractElement {

	private final Iterable<Element> elements;

	protected CompositeElement(final Iterable<Element> elements) {
		this.elements = newHashSet(elements);
	}

	public final Iterable<Element> getElements() {
		return elements;
	}

	@Override
	protected final int doHashCode() {
		return getElements().hashCode();
	}

}
