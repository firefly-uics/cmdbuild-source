package org.cmdbuild.elements;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.cmdbuild.dao.backend.postgresql.CMBackend;
import org.cmdbuild.elements.filters.AbstractFilter;
import org.cmdbuild.elements.filters.AttributeFilter;
import org.cmdbuild.elements.filters.CardFilter;
import org.cmdbuild.elements.filters.CompositeFilter;
import org.cmdbuild.elements.filters.FilterOperator;
import org.cmdbuild.elements.filters.LimitFilter;
import org.cmdbuild.elements.filters.OrderFilter;
import org.cmdbuild.elements.filters.SubQueryFilter;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.filters.FilterOperator.OperatorType;
import org.cmdbuild.elements.filters.OrderFilter.OrderFilterType;
import org.cmdbuild.elements.history.TableHistory;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.services.auth.Group;
import org.cmdbuild.services.auth.UserContext;

public class CardQueryImpl implements CardQuery {
	private static final CMBackend backend = new CMBackend();

    private ITable table;
    private boolean history; // if not null, was sought the history research
    private boolean ignoreStatus;
    private boolean count;

    private Set<String> attributes; // if null, return all attributes
    private AbstractFilter filter; // made with FilterOperator
    private LinkedList<OrderFilter> ordering; // a simple list
    private LimitFilter limit; // unique
    private String fullTextQuery; // a string to search in all the attributes
    private Integer totalRows;
    private HashMap<DirectedDomain, CardQuery> relationFilter; //to manage the ICard and IDomain for the relation filter
    private HashMap<DirectedDomain, ITable> notInRelation; //to manage the ICard and IDomain for the relation filter
	private UserContext filterNextExecutor;

    public CardQueryImpl(ITable table) {
    	this.table = table;
    	reset();
    	this.ordering = new LinkedList<OrderFilter>();
    }

    public void reset() {
    	this.history = false;
    	this.attributes = new LinkedHashSet<String>(); //to preserve order
    	this.filter = null;
    	this.relationFilter = new HashMap<DirectedDomain, CardQuery>();
    	this.notInRelation = new HashMap<DirectedDomain, ITable>();
    }

    /**
     * Cloneds only what is set by the filter
     */
    @SuppressWarnings("unchecked")
	public Object clone() {
    	CardQueryImpl cloned = new CardQueryImpl(table);
    	cloned.filter = this.filter;
    	cloned.relationFilter = (HashMap<DirectedDomain, CardQuery>) this.relationFilter.clone();
    	cloned.notInRelation = (HashMap<DirectedDomain, ITable>) this.notInRelation.clone();
    	cloned.ordering = (LinkedList<OrderFilter>) this.ordering.clone();
    	cloned.limit = (this.limit != null) ? (LimitFilter)this.limit.clone() : null;
    	cloned.count = this.count;
    	cloned.fullTextQuery = this.fullTextQuery;
    	cloned.history = this.history;
    	cloned.ignoreStatus = this.ignoreStatus;
    	cloned.totalRows = this.totalRows;
    	cloned.filterNextExecutor = this.filterNextExecutor;
    	return cloned;
    }
    
    //this should not be visible at all..
    public void setTotalRows(Integer totalRows) {
    	this.totalRows = totalRows;
    }

    /*
     * getters
     */

	public ITable getTable() {
		return table;
	}

	public boolean isHistory() {
		return history;
	}

	public boolean isIgnoreStatus() {
		return ignoreStatus;
	}

	public boolean needsCount() {
		return count;
	}

	public Set<String> getAttributes() {
		return attributes;
	}

	public AbstractFilter getFilter() {
		return filter;
	}

	public Collection<OrderFilter> getOrdering() {
		return ordering;
	}

	public LimitFilter getLimit() {
		return limit;
	}
	
	public String getFullTextQuery(){
	    return fullTextQuery;
	}
	
	public Map<DirectedDomain, CardQuery> getRelationFilter() {
	    return relationFilter;
	}
	
	public Map<DirectedDomain, ITable> getNotInRelation() {
	    return notInRelation;
	}

	/*
	 * "setters"
	 */

	public CardQuery fullText(String fullTextQuery) {
	    this.fullTextQuery = fullTextQuery;
	    return this;
	}

	public CardQuery cardNotInRelation(DirectedDomain dDomain, ITable classDestination) {	  
		if (relationFilter.containsKey(dDomain)){
			throw ORMExceptionType.ORM_FILTER_CONFLICT.createException();
		}
		if (!notInRelation.containsKey(dDomain)){
			notInRelation.put(dDomain, classDestination);		    
		} 
		return this;
	}

	public CardQuery cardInRelation(DirectedDomain dDomain, CardQuery cardQuery) {
	    if (notInRelation.containsKey(dDomain)) {
	    	throw ORMExceptionType.ORM_FILTER_CONFLICT.createException();
	    }
	    relationFilter.put(dDomain, cardQuery);
	    return this;
	}

    public CardQuery history(int idCard) {
    	if (history)
    		return this;
    	table = new TableHistory(table);
    	history = true;
    	ignoreStatus = true;
    	return this.id(idCard);    	
    }

    public CardQuery ignoreStatus() {
    	ignoreStatus = true;
    	return this;    	
    }

    public CardQuery count() {
    	count = true;
    	return this;   	
    }

    public CardQuery attributes(String... attributes) throws NotFoundException {
    	for (int i=0; i<attributes.length; i++) {
    		this.attributes.add(attributes[i]);
    	}
    	return this;
    };

    public CardQuery filter(String attributeName, AttributeFilterType filterType, Object... value) {
    	return filter(new AttributeFilter(table.getAttribute(attributeName), filterType, value));
    };

	public CardQuery filter(String attributeName, CardQuery subQuery) {
		return filter(new SubQueryFilter(table.getAttribute(attributeName), subQuery));
	}

    public CardQuery filterUpdate(String attributeName,
    		AttributeFilterType filterType, Object... value)
    		throws NotFoundException {
    	if(filter == null) {
    		filter = new AttributeFilter(table.getAttribute(attributeName), filterType, value);
    	} else {
    		AttributeFilter oldFlt = search(attributeName,filter);
    		if(oldFlt == null) {
    			//not found
    			return filter(new AttributeFilter(table.getAttribute(attributeName), filterType, value));
    		} else {
    			// update type and values
    			oldFlt.setFilterType(filterType);
    			oldFlt.setValues(value);
    		}
    	}
    	return this;
    }

    private AttributeFilter search( String attrName, AbstractFilter flt ) {
    	if(flt instanceof AttributeFilter) {
    		AttributeFilter af = (AttributeFilter)flt;
    		if(af.getAttributeName().equals(attrName)) {
    			return af;
    		} else {
    			return null;
    		}
    	} else if( flt instanceof FilterOperator ) {
    		FilterOperator fltOp = (FilterOperator)flt;
    		AttributeFilter out = null;
    		for(AbstractFilter f : fltOp.getExpressions()) {
    			if(null != (out = search(attrName,f))) {
    				return out;
    			}
    		}
    		return null;
    	} else if( flt instanceof CompositeFilter ) {
    		CompositeFilter compFlt = (CompositeFilter)flt;
    		AttributeFilter out = null;
    		for(CompositeFilter.CompositeFilterItem item : compFlt.getItems()) {
    			if(null != (out = search(attrName,item.getFilter()))) {
    				return out;
    			}
    		}
    	}
    	return null;
    }

    //@Deprecated ****** should be private
    public CardQuery filter(AbstractFilter filterCriteria) {
    	if (filter == null) {
    		filter = filterCriteria;
    	} else {
    		LinkedList<AbstractFilter> subFilters = new LinkedList<AbstractFilter>();
    		subFilters.add(filter);
    		subFilters.add(filterCriteria);
    		filter = new FilterOperator(OperatorType.AND, subFilters);
    	}
    	return this;
    };

    public CardQuery id(int idCard) {
    	try {
    		return this.filter("Id", AttributeFilterType.EQUALS, String.valueOf(idCard));
    	} catch (NotFoundException e) {
    		return this;
    	}
    };

    public CardQuery cards(Iterable<ICard> cards) {
    	try {
    		return this.filter(new CardFilter(this.table, cards));
    	} catch (NotFoundException e) {
    		return this;
    	}
    };

    public CardQuery order(String attributeName, OrderFilterType type) throws NotFoundException {
    	this.ordering.add(new OrderFilter(table.getAttribute(attributeName), type));
    	return this;
    };

	public CardQuery clearOrder() {
		this.ordering = new LinkedList<OrderFilter>();
		return this;
	}

    @Deprecated
    public CardQuery order(OrderFilter[] orderCriteriaArray) {
    	for (int i=0; i<orderCriteriaArray.length; i++)
    		this.ordering.add(orderCriteriaArray[i]);
    	return this;
    };

    public CardQuery subset(int offset, int limit) {
    	if (offset >= 0 && limit > 0)
    		this.limit = new LimitFilter(offset, limit);
    	return this;
    };

    public CardQuery limit(int limit) {
    	if (limit > 0)
    		this.limit = new LimitFilter(0, limit);
    	else
    		this.limit = null;
    	return this;
    };

	public ICard get() throws NotFoundException {
		return get(true);
	}

	public ICard get(boolean ignoreStatus) throws NotFoundException {
		try {
			if (ignoreStatus) {
				ignoreStatus();
			}
			return iterator().next();
		} catch (NoSuchElementException e) {
			throw NotFoundExceptionType.CARD_NOTFOUND.createException(this.getTable().getName());
		}
	}

	public Integer getTotalRows() {
		return totalRows;
	}

	public Iterator<ICard> iterator() {
		totalRows = null;
		return backend.getCardList(this).iterator();
	}

	public void update(ICard cardTemplate) {
		backend.updateCardsFromTemplate(this, cardTemplate);
	}

	/**
	 * Returns the card position within the current filtering 
	 * 
	 * @param cardId
	 * @return position
	 */
	public int position(int cardId) throws NotFoundException {
		return backend.getCardPosition(this, cardId);
	}

	/*
	 * I'm not proud of this. Should be refactored completely in the next few weeks.
	 */
	public void setNextExecutorFilter(UserContext userCtx) {
		filterNextExecutor = userCtx;			
	}

	public boolean needsNextExecutorFilter() {
		return (filterNextExecutor != null && !filterNextExecutor.privileges().isAdmin());
	}

	public  Collection<Group> getExecutorFilterGroups() {
		return filterNextExecutor.getGroups();
	}
}
