package org.cmdbuild.dao.query;

/*
 * Immutable interface to mask result object building
 */
public interface CMQueryResult extends Iterable<CMQueryRow> {

	public int size();
	public boolean isEmpty();
	public int totalSize();
}
