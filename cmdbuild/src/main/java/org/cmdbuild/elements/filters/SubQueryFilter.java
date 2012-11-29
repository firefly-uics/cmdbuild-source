package org.cmdbuild.elements.filters;

import java.util.Map;

import org.cmdbuild.dao.backend.postgresql.CardQueryBuilder;
import org.cmdbuild.dao.backend.postgresql.QueryComponents.QueryAttributeDescriptor;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.IAttribute;

public class SubQueryFilter extends AbstractFilter {

	private static final long serialVersionUID = 1L;

	private IAttribute attribute;
	private CardQuery subQuery;

    public SubQueryFilter(IAttribute attribute, CardQuery subQuery) {
		this.attribute = attribute;
		this.subQuery = subQuery;
    }

	@Override
	public String toString(Map<String, QueryAttributeDescriptor> queryMapping) {
		if (queryMapping != null && queryMapping.containsKey(attribute.getName())) {
			return toString(queryMapping.get(attribute.getName()).getValueName() );
		} else {
			return toString("\"" + attribute.getSchema().getDBName() + "\".\"" +  attribute.getName() + "\"");
		}
	}

	private String toString(String fullName) {
		final String subSelect = new CardQueryBuilder().buildSelectQuery(subQuery);
		return String.format("%s IN (%s)", fullName, subSelect);
	}
}
