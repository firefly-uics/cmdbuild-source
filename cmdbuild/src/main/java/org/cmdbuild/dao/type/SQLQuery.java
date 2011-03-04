package org.cmdbuild.dao.type;

/*
 * FIXME Direct SQL queries should be removed
 */
public class SQLQuery {

	private final String queryString;

	public SQLQuery(String queryString) {
		this.queryString = queryString;
	}

	public String toString() {
		return queryString;
	}
}
