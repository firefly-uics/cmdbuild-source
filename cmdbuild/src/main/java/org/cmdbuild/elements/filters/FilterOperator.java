package org.cmdbuild.elements.filters;

import java.util.Iterator;
import java.util.Map;

import org.cmdbuild.dao.backend.postgresql.QueryComponents.QueryAttributeDescriptor;
import org.cmdbuild.utils.StringUtils;

public class FilterOperator extends AbstractFilter {

	private static final long serialVersionUID = 1L;

	public enum OperatorType {
		AND("AND"), OR("OR"), NOT("NOT");

		private final String operatorStr;
		OperatorType(String operatorStr) { this.operatorStr = operatorStr; }
	    public String toString() { return operatorStr; }
	}
	
	private OperatorType type;
	private Iterable<AbstractFilter> expressions;
	
	public FilterOperator(OperatorType type, Iterable<AbstractFilter> expressions) {
		this.type = type;
		this.expressions = expressions;
	}
	
	public String toString(Map<String, QueryAttributeDescriptor> queryMapping) {
		StringBuffer rv = new StringBuffer();
		if (type.equals(OperatorType.NOT))
			rv.append(type + " " + expressions.iterator().next().toString(queryMapping));
		else {
			String delimiter = " " + type.toString() + " ";
			Iterator<AbstractFilter> iter = expressions.iterator();
			while (iter.hasNext()) {
				AbstractFilter f = iter.next();
				if (f != null) {
					if (rv.length() != 0) {
						rv.append(delimiter);
					}
					rv.append(f.toString(queryMapping));
				}
			}
		}
		return "("+rv.toString()+")";
	}
	
	public Iterable<AbstractFilter> getExpressions() {
		return this.expressions;
	}

	public String toString() {
		String rv = "( ";
		if(type.equals(OperatorType.NOT))
			rv += type + " " + expressions.iterator().next().toString();
		else
			rv += StringUtils.join(expressions.iterator(), " " + type.toString() + " ");
		rv += " )";
		
		return rv;
	}
}
