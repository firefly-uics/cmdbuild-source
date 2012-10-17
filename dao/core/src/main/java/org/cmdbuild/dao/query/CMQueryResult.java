package org.cmdbuild.dao.query;

import java.util.NoSuchElementException;

/*
 * Immutable interface to mask result object building
 */
public interface CMQueryResult extends Iterable<CMQueryRow> {

	int size();

	boolean isEmpty();

	int totalSize();

	/**
	 * Returns the first and only row in the result.
	 * 
	 * @return the first and only row in the result
	 * @throws NoSuchElementException
	 *             if there is no unique element
	 */
	CMQueryRow getOnlyRow() throws NoSuchElementException;

}
