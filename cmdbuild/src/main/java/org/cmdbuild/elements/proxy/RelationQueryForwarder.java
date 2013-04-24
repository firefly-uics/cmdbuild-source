package org.cmdbuild.elements.proxy;

import java.util.Iterator;
import java.util.Set;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.filters.LimitFilter;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.interfaces.RelationQuery;
import org.cmdbuild.elements.utils.CountedValue;

@OldDao
@Deprecated
public class RelationQueryForwarder implements RelationQuery {
	RelationQuery relationQuery;

	protected RelationQueryForwarder(RelationQuery relationQuery) {
		this.relationQuery = relationQuery;
	}

	public RelationQuery card(ICard card) { relationQuery.card(card); return this; }
	public RelationQuery domain(DirectedDomain directedDomain) { relationQuery.domain(directedDomain); return this; }
	public RelationQuery domain(DirectedDomain directedDomain, boolean fullCards) { relationQuery.domain(directedDomain, fullCards); return this; };
	public RelationQuery domain(IDomain domain) { relationQuery.domain(domain); return this; }
	public RelationQuery history() { relationQuery.history(); return this; }
	public RelationQuery domainLimit(int limit) { relationQuery.domainLimit(limit); return this; }
	public RelationQuery orderByDomain() { relationQuery.orderByDomain(); return this; }
	public RelationQuery straightened() { relationQuery.straightened(); return this; }
	public RelationQuery subset(int start, int limit) { relationQuery.subset(start, limit); return this; }

	public Set<ICard> getCards() { return relationQuery.getCards(); }
	public Set<DirectedDomain> getDomains() { return relationQuery.getDomains(); }
	public int getDomainLimit() { return relationQuery.getDomainLimit(); }
	public DirectedDomain getFullCardsDomain() { return relationQuery.getFullCardsDomain(); }
	public boolean isDomainCounted() { return relationQuery.isDomainCounted(); }
	public boolean isHistory() { return relationQuery.isHistory(); }
	public boolean isDomainLimited() { return relationQuery.isDomainLimited(); }
	public boolean isOrderedByDomain() { return relationQuery.isOrderedByDomain(); }
	public boolean isFullCards() { return relationQuery.isFullCards(); }
	public boolean isStraightened() { return relationQuery.isStraightened(); }

	public LimitFilter getLimit() { return relationQuery.getLimit();}
	public Iterable<CountedValue<IRelation>> getCountedIterable() { return relationQuery.getCountedIterable(); }
	public Iterable<IRelation> getIterable() { return relationQuery.getIterable(); }
	public Iterator<IRelation> iterator() { return relationQuery.iterator(); }
}
