package org.cmdbuild.elements.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.backend.postgresql.QueryComponents.QueryAttributeDescriptor;
import org.cmdbuild.elements.filters.FilterOperator.OperatorType;

/**
 * A filter in which is possible to add other subfilters, linked with AND, OR
 */
public class CompositeFilter extends AbstractFilter {
	/**
	 * A filter with a preposition of AND, OR or NOT
	 */
	public class CompositeFilterItem extends AbstractFilter {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		AbstractFilter flt;
		FilterOperator.OperatorType type;
		public CompositeFilterItem(OperatorType type, AbstractFilter filter) {
			this.type = type;
			this.flt = filter;
		}
		
		public AbstractFilter getFilter(){return flt;}
		
		@Override
		public String toString(
				Map<String, QueryAttributeDescriptor> queryMapping) {
			if(type == null) {
				return flt.toString(queryMapping);
			}
			return type.toString() + " " + flt.toString(queryMapping);
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	List<CompositeFilterItem> items = new ArrayList<CompositeFilterItem>();
	boolean group = true;
	
	public List<CompositeFilterItem> getItems() {
		return items;
	}
	
	public void setGroup(boolean group) {
		this.group = group;
	}
	
	public boolean hasItems() {
		return !items.isEmpty();
	}
	
	public CompositeFilterItem createItem(OperatorType type, AbstractFilter flt){
		return new CompositeFilterItem(type,flt);
	}
	
	public CompositeFilter and(boolean not,AbstractFilter flt) {
		return not ? andNot(flt) : and(flt);
	}
	public CompositeFilter or(boolean not,AbstractFilter flt) {
		return not ? orNot(flt) : or(flt);
	}
	
	public CompositeFilter first(boolean not,AbstractFilter flt) {
		items.add(createItem( (not?OperatorType.NOT:null),flt));
		return this;
	}
	public CompositeFilter and(AbstractFilter flt) {
		items.add(new CompositeFilterItem(OperatorType.AND, flt));
		return this;
	}
	public CompositeFilter or(AbstractFilter flt) {
		items.add(new CompositeFilterItem(OperatorType.OR, flt));
		return this;
	}
	public CompositeFilter andNot(AbstractFilter flt) {
		items.add(createItem(OperatorType.AND,createItem(OperatorType.NOT,flt)));
		return this;
	}
	public CompositeFilter orNot(AbstractFilter flt) {
		items.add(createItem(OperatorType.OR,createItem(OperatorType.NOT,flt)));
		return this;
	}

	@Override
	public String toString(Map<String, QueryAttributeDescriptor> queryMapping) {
		StringBuffer sbuf = new StringBuffer();
		if(group) sbuf.append('(');
		for(CompositeFilterItem item : items) {
			sbuf.append(item.toString(queryMapping));
		}
		if(group) sbuf.append(')');
		return sbuf.toString();
	}

}
