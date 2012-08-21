package org.cmdbuild.elements.filters;

import java.util.Iterator;
import java.util.Map;

import org.cmdbuild.dao.backend.postgresql.QueryComponents.QueryAttributeDescriptor;
import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;

public class FullTextFilter extends AbstractFilter {

	private static final long serialVersionUID = 1L;

    private String fullTextQuery = null;

    public FullTextFilter(String fullTextQuery) {
    	this.fullTextQuery = fullTextQuery;
    }

    @Override
    public String toString(Map<String, QueryAttributeDescriptor> queryMapping) {
    	return formatToString(queryMapping,"ROW",",","","");
    }

    private String formatToString(Map<String, QueryAttributeDescriptor> queryMapping, String start, String separator, String preValue, String postValue) {
    	StringBuffer queryText = new StringBuffer(start).append("(");
    	Iterator<QueryAttributeDescriptor> i = queryMapping.values().iterator();
    	boolean afterFirst = false;
    	while (i.hasNext()) {
    		QueryAttributeDescriptor descriptor = i.next();
    		String descriptionName = descriptor.getDescriptionName();
    		if (descriptionName == null)
    			continue;
    		if (afterFirst) {
    			queryText.append(separator);
    		} else {
    			afterFirst = true;
    		}
    		queryText.append(preValue).append(descriptionName).append(postValue);
    	}
    	queryText.append(")::TEXT ILIKE \'%").append(quotedValue()).append("%\'");
    	return queryText.toString();
    }

    private String quotedValue() {
    	return AttributeFilterType.LIKE.escapeValue(AttributeImpl.pgEscape(fullTextQuery));
    }
}
