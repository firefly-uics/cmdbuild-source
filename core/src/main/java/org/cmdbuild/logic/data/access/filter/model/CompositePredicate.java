package org.cmdbuild.logic.data.access.filter.model;

import static com.google.common.collect.Sets.newHashSet;

abstract class CompositePredicate extends AbstractPredicate {

	private final Iterable<Predicate> predicates;

	protected CompositePredicate(final Iterable<Predicate> elements) {
		this.predicates = newHashSet(elements);
	}

	public Iterable<Predicate> getPredicates() {
		return predicates;
	}

	@Override
	protected int doHashCode() {
		return getPredicates().hashCode();
	}

}
