package org.cmdbuild.elements.interfaces;

import java.util.Iterator;
import java.util.Set;

import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.filters.LimitFilter;
import org.cmdbuild.elements.utils.CountedValue;

public interface RelationQuery extends Iterable<IRelation> {

	/**
	 * Search for relation also in the history
	 * 
	 * @return the filter itself for chaining
	 */
	public RelationQuery history();

	/**
	 * Returns the relations reversed depending on the relation direction
	 * 
	 * @return the filter itself for chaining
	 */
	public RelationQuery straightened();
	public RelationQuery card(ICard card);
	public RelationQuery domain(DirectedDomain directedDomain);
	public RelationQuery domain(DirectedDomain directedDomain, boolean fullCards);
	public RelationQuery domain(IDomain domain);
	public RelationQuery domainLimit(int limit);
	public RelationQuery subset(int start, int limit);
	public RelationQuery orderByDomain();

	public Set<ICard> getCards();
	public Set<DirectedDomain> getDomains();
	public DirectedDomain getFullCardsDomain();
	public int getDomainLimit();

	public boolean isHistory();
	public boolean isDomainCounted();
	public boolean isDomainLimited();
	public boolean isOrderedByDomain();
	public boolean isFullCards();
	public boolean isStraightened();

	public Iterator<IRelation> iterator();
	public Iterable<IRelation> getIterable();
	public Iterable<CountedValue<IRelation>> getCountedIterable();

	public LimitFilter getLimit();
}
