package org.cmdbuild.services.soap.types;

import java.util.List;

/**
 * Lista di subquery con un operatore.... se list = Q1, Q2, Q3 e l'operatore è
 * AND, verrà Q1 AND Q2 AND Q3 (composite where clause)
 */
public class FilterOperator {

	private String operator;
	private List<Query> subquery;

	public FilterOperator() {
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public List<Query> getSubquery() {
		return subquery;
	}

	public void setSubquery(List<Query> subquery) {
		this.subquery = subquery;
	}

}
