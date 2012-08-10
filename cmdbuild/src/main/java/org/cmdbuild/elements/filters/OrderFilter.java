package org.cmdbuild.elements.filters;

import java.util.Map;

import org.cmdbuild.dao.backend.postgresql.QueryComponents.QueryAttributeDescriptor;
import org.cmdbuild.elements.interfaces.IAttribute;

public class OrderFilter {

	private static final long serialVersionUID = 1L;

	public enum OrderFilterType {
		ASC("ASC"), DESC("DESC");
		private final String operatorStr;
		OrderFilterType(String operatorStr) { this.operatorStr = operatorStr; }
	    public String toString()   { return operatorStr; }
	}

	private IAttribute attribute;
	private OrderFilterType filterType;

	public OrderFilter(IAttribute attribute, OrderFilterType filterType){
		this.attribute = attribute;
		this.filterType = filterType;
	}

	public String getAttributeName() {
		return attribute.getName();
	}

	public String toString(Map<String, QueryAttributeDescriptor> queryMapping) {
		if (queryMapping != null) {
			QueryAttributeDescriptor qad = queryMapping.get(attribute.getName());
			String fieldName = qad.getOrderingName();
			return toString(fieldName);
		} else {
			return toString("\"" + attribute.getSchema().getDBName() + "\".\"" +  attribute.getName() + "\"");
		}
	}

	private String toString(String fullName){
		return fullName + " " + filterType;
	}
}
