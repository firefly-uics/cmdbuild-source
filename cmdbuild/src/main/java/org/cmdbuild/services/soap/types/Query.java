package org.cmdbuild.services.soap.types;

import java.util.List;
import java.util.Vector;

import org.cmdbuild.elements.filters.AbstractFilter;
import org.cmdbuild.elements.filters.AttributeFilter;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.filters.FilterOperator.OperatorType;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logger.Log;

public class Query {
	
	private Filter filter;
	private FilterOperator filterOperator;
	
	public Query() { } 
	
	public Filter getFilter() {
		return filter;
	}
	public void setFilter(Filter filter) {
		this.filter = filter;
	}
	public FilterOperator getFilterOperator() {
		return filterOperator;
	}
	public void setFilterOperator(FilterOperator filterOperator) {
		this.filterOperator = filterOperator;
	}
	
	public AbstractFilter toAbstractFilter(ITable table) throws NotFoundException{
		AbstractFilter retval = null; 
		Filter filter = this.getFilter();
		FilterOperator filterOperator = this.getFilterOperator();

		if (filter != null) {
			AttributeFilterType operator = AttributeFilterType.valueOf(AttributeFilterType.class, filter.getOperator());
			org.cmdbuild.elements.interfaces.IAttribute attribute;
			attribute = table.getAttribute(filter.getName());
			List<String> filterValues = filter.getValue();
			String[] value = filterValues.toArray(new String[filterValues.size()]);
			StringBuffer values = new StringBuffer();
		    if (value.length > 0) {
		    	values.append(value[0]);
		        for (int i=1; i < value.length; i++) {
		        	values.append(", ");
		        	values.append(value[i]);
		        }
		    }
			Log.SOAP.debug("Applying following filter: " + filter.getName() + " " + filter.getOperator() + " (" + values.toString() + ")");
			retval = new AttributeFilter(attribute, operator, (Object[])value);
		} else if (filterOperator != null){
			List<AbstractFilter> subFilters = new Vector<AbstractFilter>();
			for (Query subquery : filterOperator.getSubquery()) {
				subFilters.add(toAbstractFilter(subquery, table));
			}
			OperatorType operatorType = OperatorType.valueOf(OperatorType.class, filterOperator.getOperator());
			retval = new org.cmdbuild.elements.filters.FilterOperator(operatorType, subFilters);
		}
		return retval;
	}
	
	private AbstractFilter toAbstractFilter(Query query, ITable table) throws NotFoundException{
		AbstractFilter retval = null; 
		Filter filter = query.getFilter();
		FilterOperator filterOperator = query.getFilterOperator();

		if (filter != null) {
			AttributeFilterType operator = AttributeFilterType.valueOf(AttributeFilterType.class, filter.getOperator());
			org.cmdbuild.elements.interfaces.IAttribute attribute;
			attribute = table.getAttribute(filter.getName());
			List<String> filterValues = filter.getValue();
			String[] value = filterValues.toArray(new String[filterValues.size()]);
			StringBuffer values = new StringBuffer();
		    if (value.length > 0) {
		    	values.append(value[0]);
		        for (int i=1; i < value.length; i++) {
		        	values.append(", ");
		        	values.append(value[i]);
		        }
		    }
		    Log.SOAP.debug("Applying following filter: " + filter.getName() + " " + filter.getOperator() + " (" + values.toString() + ")");
			retval = new AttributeFilter(attribute, operator, (Object[])value);
		} else if (filterOperator != null){
			List<AbstractFilter> subFilters = new Vector<AbstractFilter>();
			for (Query subquery : filterOperator.getSubquery()) {
				subFilters.add(toAbstractFilter(subquery, table));
			}
			OperatorType operatorType = OperatorType.valueOf(OperatorType.class, filterOperator.getOperator());
			retval = new org.cmdbuild.elements.filters.FilterOperator(operatorType, subFilters);
		}
		return retval;
	}
	
}
