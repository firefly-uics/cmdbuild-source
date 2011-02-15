package org.cmdbuild.elements.interfaces;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.filters.AbstractFilter;
import org.cmdbuild.elements.filters.LimitFilter;
import org.cmdbuild.elements.filters.OrderFilter;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.filters.OrderFilter.OrderFilterType;
import org.cmdbuild.elements.wrappers.AvailableMenuItemsView;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.services.auth.Group;
import org.cmdbuild.services.auth.UserContext;

public interface CardQuery extends Iterable<ICard>, Cloneable {

	public CardQuery id(int idCard);
    public CardQuery clearOrder();
	public CardQuery order(String attributeName, OrderFilterType type);
	public CardQuery subset(int offset, int limit);
	public CardQuery limit(int limit);
	public CardQuery fullText(String fullTextQuery);
	public CardQuery cardInRelation(DirectedDomain dDomain, CardQuery cardQuery);
	public CardQuery cardNotInRelation(DirectedDomain dDomain, ITable classDestination);
	public CardQuery ignoreStatus();
	public CardQuery count();
	public CardQuery history(int idCard);
	public CardQuery attributes(String... attributes);
	public CardQuery filter(AbstractFilter filterCriteria);
	public CardQuery filter(String attributeName, AttributeFilterType filterType, Object... value);
	public CardQuery filter(String attributeName, CardQuery subQuery);
	public CardQuery filterUpdate(String attributeName, AttributeFilterType filterType, Object... value);

	public void setNextExecutorFilter(UserContext userCtx); // Horrible: as usual we are in a hurry
	public boolean needsNextExecutorFilter();
	public Collection<Group> getExecutorFilterGroups();

    @Deprecated
    public CardQuery order(OrderFilter[] orderCriteriaArray);

	public void reset();
	public ITable getTable();
	public boolean isHistory();
	public Set<String> getAttributes();
	public Map<DirectedDomain, CardQuery> getRelationFilter();
	public Map<DirectedDomain, ITable> getNotInRelation();
	public AbstractFilter getFilter();
	public Collection<OrderFilter> getOrdering();
	public LimitFilter getLimit();
	public String getFullTextQuery();
	public Integer getTotalRows();
	public int position(int cardId);

	public CardQuery cards(Iterable<ICard> cards);
	public ICard get() throws NotFoundException;
	public ICard get(boolean ignoreStatus);

	public void update(ICard cardTemplate);

	/**
	 * Ignores the status attribute
	 * 
	 * It is used when requesting cards from phoney classes that query views
	 * like for the menu items not used in the selected menu.
	 * 
	 * @see AvailableMenuItemsView
	 * 
	 * @return itself
	 */
	public boolean isIgnoreStatus();
	public boolean needsCount();

	public Object clone();
}
