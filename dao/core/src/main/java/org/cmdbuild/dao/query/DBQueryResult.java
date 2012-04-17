package org.cmdbuild.dao.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/*
 * Mutable classes used by the driver implementations
 */
public class DBQueryResult implements CMQueryResult {

	Collection<CMQueryRow> rows;
	int totalSize;

	public DBQueryResult() {
		rows = new ArrayList<CMQueryRow>();
		totalSize = 0;
	}

	public void add(final CMQueryRow row) {
		rows.add(row);
	}

	public int getAndIncrementTotalSize() {
		return totalSize++;
	}

	@Override
	public Iterator<CMQueryRow> iterator() {
		return rows.iterator();
	}

	@Override
	public int size() {
		return rows.size();
	}

	@Override
	public boolean isEmpty() {
		return rows.isEmpty();
	}

	@Override
	public int totalSize() {
		return totalSize;
	}
}
