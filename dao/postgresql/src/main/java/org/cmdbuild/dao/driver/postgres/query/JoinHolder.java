package org.cmdbuild.dao.driver.postgres.query;

import org.cmdbuild.dao.query.clause.alias.Alias;

/**
 * Holds the join elements needed queries over superclasses.
 */
interface JoinHolder {

	interface JoinElement {

		Alias getFrom();

		Alias getTo();

	}

	void add(Alias from, Alias to);

	Iterable<JoinElement> getElements();

}
