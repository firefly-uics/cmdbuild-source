package org.cmdbuild.elements.proxy;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.filters.AbstractFilter;
import org.cmdbuild.elements.filters.LimitFilter;
import org.cmdbuild.elements.filters.OrderFilter;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.filters.OrderFilter.OrderFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.services.auth.Group;
import org.cmdbuild.services.auth.UserContext;

public class CardQueryForwarder implements CardQuery {
	protected CardQuery cardQuery;

	protected CardQueryForwarder(CardQuery cardQuery) {
		this.cardQuery = cardQuery;
	}

	public CardQuery attributes(String... attributes) { cardQuery.attributes(attributes); return this; }
	public CardQuery cardInRelation(DirectedDomain domain,
			CardQuery cardQuery) { this.cardQuery.cardInRelation(domain, cardQuery); return this; }
	public CardQuery cardNotInRelation(DirectedDomain domain,
			ITable classDestination) { cardQuery.cardNotInRelation(domain, classDestination); return this; }
	public CardQuery cards(Iterable<ICard> cards) { cardQuery.cards(cards); return this; }
	public CardQuery excludeCards(Iterable<ICard> cards) { cardQuery.excludeCards(cards); return this; }
	public CardQuery clearOrder() { cardQuery.clearOrder(); return this; }
	public CardQuery count() { cardQuery.count(); return this; }
	public CardQuery filter(AbstractFilter filterCriteria) { cardQuery.filter(filterCriteria); return this; }
	public CardQuery filter(String attributeName, AttributeFilterType filterType, Object... value) {
		cardQuery.filter(attributeName, filterType, value); return this;
	}
	public CardQuery filter(String attributeName, CardQuery subQuery) {
		cardQuery.filter(attributeName, subQuery); return this;
	}
	public CardQuery filterUpdate(String attributeName, AttributeFilterType filterType, Object... value) {
		cardQuery.filterUpdate(attributeName, filterType, value); return this;
	}
	public boolean needsPrevExecutorsFilter() { return cardQuery.needsPrevExecutorsFilter(); }
	public void setPrevExecutorsFilter(UserContext userCtx) { cardQuery.setPrevExecutorsFilter(userCtx); }
	public Collection<Group> getExecutorFilterGroups() { return cardQuery.getExecutorFilterGroups(); };

	public CardQuery fullText(String fullTextQuery) { cardQuery.fullText(fullTextQuery); return this; }
	public CardQuery limit(int limit) { cardQuery.limit(limit); return this; }
	@SuppressWarnings("deprecation")
	public CardQuery order(OrderFilter[] orderCriteriaArray) { cardQuery.order(orderCriteriaArray); return this; }
	public CardQuery order(String attributeName, OrderFilterType type) { cardQuery.order(attributeName, type); return this; }
	public CardQuery subset(int offset, int limit) { cardQuery.subset(offset, limit); return this; }
	public CardQuery history(int idCard) { cardQuery.history(idCard); return this; }
	public CardQuery id(int idCard) { cardQuery.id(idCard); return this; }
	public CardQuery ignoreStatus() { cardQuery.ignoreStatus(); return this; }
	
	public ICard get() { return cardQuery.get(); }
	public ICard get(boolean ignoreStatus) { return cardQuery.get(ignoreStatus); }

	public Set<String> getAttributes() { return cardQuery.getAttributes(); }
	public AbstractFilter getFilter() { return cardQuery.getFilter(); }
	public String getFullTextQuery() { return cardQuery.getFullTextQuery(); }
	public LimitFilter getLimit() { return cardQuery.getLimit(); }
	public Map<DirectedDomain, ITable> getNotInRelation() { return cardQuery.getNotInRelation(); }
	public Collection<OrderFilter> getOrdering() { return cardQuery.getOrdering(); }
	public Map<DirectedDomain, CardQuery> getRelationFilter() { return cardQuery.getRelationFilter(); };
	public ITable getTable() { return cardQuery.getTable(); }
	public Integer getTotalRows() { return cardQuery.getTotalRows(); }
	public boolean isHistory() { return cardQuery.isHistory(); }
	public boolean isIgnoreStatus() { return cardQuery.isIgnoreStatus(); }
	public Iterator<ICard> iterator() { return cardQuery.iterator(); }
	public boolean needsCount() { return cardQuery.needsCount(); }

	public int position(int cardId) { return cardQuery.position(cardId); }
	public void reset() { cardQuery.reset(); }
	public void update(ICard cardTemplate) { cardQuery.update(cardTemplate); }
	public Object clone() { return cardQuery.clone(); }
}
