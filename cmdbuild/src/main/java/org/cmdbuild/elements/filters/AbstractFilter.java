package org.cmdbuild.elements.filters;

import java.io.Serializable;
import java.util.Map;

import org.cmdbuild.dao.backend.postgresql.QueryComponents.QueryAttributeDescriptor;

public abstract class AbstractFilter implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Map<String, QueryAttributeDescriptor> queryMapping;

	public void setQueryMapping(Map<String, QueryAttributeDescriptor> queryMapping) {
		this.queryMapping = queryMapping;
	}

	public abstract String toString(Map<String, QueryAttributeDescriptor> queryMapping);

	public String toString() {
		return toString(this.queryMapping);
	}
}
