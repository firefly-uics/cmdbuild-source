package org.cmdbuild.elements.proxy;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.cmdbuild.elements.TableImpl;
import org.cmdbuild.elements.TableTree;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.services.auth.UserContext;

public class CardQueryProxy extends CardQueryForwarder {
	protected UserContext userCtx;
	private Integer totalRows;

	class CardProxyIterator implements Iterator<ICard> {
		Iterator<ICard> i;
		CardProxyIterator(Iterator<ICard> i) {
			this.i = i;
		}
		public ICard next() {
			return new CardProxy(i.next(),
					CardQueryProxy.this.userCtx);
		}
		public boolean hasNext() { return i.hasNext(); }
		public void remove() { i.remove(); }
	}
	
	public CardQueryProxy(CardQuery cardQuery, UserContext userCtx) {
		super(cardQuery);
		this.userCtx = userCtx;
	}

	public Object clone() {
		CardQuery clonedCardQuery = (CardQuery) cardQuery.clone();
		return new CardQueryProxy(clonedCardQuery, userCtx);
	}

	@Override
	public ICard get() {
		return new CardProxy(super.get(), userCtx);
	}

	/*
	 * NdFrancesco:
	 * Does it make sense to clone the card query in another variable?
	 * normally, the iterator() call is the last one would call.
	 * 
	 * setTotalRows is a workaround: cast to CardQuery --we
	 * really don't want the setTotalRows method to be in the
	 * interface-- and then set the totalRows which are in the newly
	 * created clonedCardQuery...
	 * 
	 * ..but if it were for me, I will have cloned the card query in itself,
	 * and then check if it needs further manipulations somehow
	 *    cardQuery = (ICardQuery)cardQuery.clone();
	 *    removeUnprivilegedSubclasses(cardQuery);
	 *    return new CardProxyIterator(cardQuery.iterator());
	 */
	@Override
	public Iterator<ICard> iterator() {
		Iterator<ICard> out;
		if (cardQuery.getTable().isSuperClass()) {
			CardQuery clonedCardQuery = (CardQuery) cardQuery.clone();
			removeUnprivilegedSubclasses(clonedCardQuery);
			out = new CardProxyIterator(clonedCardQuery.iterator());
			this.totalRows = clonedCardQuery.getTotalRows();
		} else {
			out = new CardProxyIterator(cardQuery.iterator());
		}
		return out;
	}

	@Override
	public Integer getTotalRows() {
		if (totalRows != null) {
			return totalRows;
		} else {
			return cardQuery.getTotalRows();
		}
	}

	@Override
	public void update(ICard cardTemplate) {
		CardQuery clonedCardQuery = (CardQuery) cardQuery.clone();
		removeReadOnlySubclasses(clonedCardQuery);
		cardTemplate.setUser(userCtx.getUsername());
		clonedCardQuery.update(cardTemplate);
	}

	private void removeUnprivilegedSubclasses(CardQuery cardQuery) {
		List<String> noPrivTables = new LinkedList<String>();
		TableTree wholeTree = TableImpl.tree().branch(getTable().getName());
		for (ITable table : wholeTree) {
			if (!userCtx.privileges().hasReadPrivilege(table))
				noPrivTables.add(String.valueOf(table.getId()));
		}
		if (!noPrivTables.isEmpty()) {
			String[] noPrivTablesArray = noPrivTables.toArray(new String[0]);
			cardQuery.filter(ICard.CardAttributes.ClassId.toString(), AttributeFilterType.DIFFERENT, (Object[])noPrivTablesArray);
		}
	}

	private void removeReadOnlySubclasses(CardQuery cardQuery) {
		List<String> noWriteTables = new LinkedList<String>();
		TableTree wholeTree = TableImpl.tree().branch(getTable().getName());
		for (ITable table : wholeTree) {
			if (!userCtx.privileges().hasWritePrivilege(table))
				noWriteTables.add(String.valueOf(table.getId()));
		}
		if (!noWriteTables.isEmpty()) {
			String[] noPrivTablesArray = noWriteTables.toArray(new String[0]);
			cardQuery.filter(ICard.CardAttributes.ClassId.toString(), AttributeFilterType.DIFFERENT, (Object[])noPrivTablesArray);
		}
	}
}
